package br.rafaeros.smp.modules.infra.tcp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.rafaeros.smp.modules.device.model.enums.ProcessStatus;
import br.rafaeros.smp.modules.device.service.DeviceService;
import br.rafaeros.smp.modules.log.controller.dto.DeviceLogPayloadDTO;
import br.rafaeros.smp.modules.log.service.LogService;
import br.rafaeros.smp.modules.order.model.Order;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TcpServerService {

    private final DeviceService deviceService;
    private final LogService logService;
    private final ObjectMapper objectMapper;
    
    // Virtual Threads (Java 21+)
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private ServerSocket serverSocket;
    private volatile boolean running = true;

    // Server Protection Settings
    private static final int MAX_LINE_LENGTH = 2048; // 2KB line limit to prevent OutOfMemory
    private static final long RATE_LIMIT_MS = 500;   // Minimum threshold between heavy tasks

    // Connection and rate limiting management
    private final ConcurrentHashMap<String, Socket> activeConnections = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastActionTimes = new ConcurrentHashMap<>();

    @PostConstruct
    public void startServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(5050);
                log.info("🚀 TCP Server running on port 5050 (Optimized for 1000+ devices)");

                while (running && !serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(() -> handleClient(clientSocket));
                }
            } catch (IOException e) {
                if (running) log.error("Fatal error in TCP server", e);
            }
        }).start();
    }

    private void handleClient(Socket deviceSocket) {
        String macAddress = null;
        String clientIp = deviceSocket.getInetAddress().getHostAddress();
        
        try {
            deviceSocket.setSoTimeout(15000); // 15s idle timeout

            InputStream inputStream = deviceSocket.getInputStream();
            PrintWriter writer = new PrintWriter(deviceSocket.getOutputStream(), true);
            
            // Initial handshake
            String firstLine = safeReadLine(inputStream);
            if (firstLine == null) return;

            if (firstLine.startsWith("ID:")) {
                String[] parts = firstLine.split("ID:");
                if (parts.length > 1) {
                    macAddress = parts[1].trim();
                    
                    // Ensure old connections for the same MAC are closed
                    killZombieConnection(macAddress, deviceSocket);
                    
                    activeConnections.put(macAddress, deviceSocket);
                    deviceService.registerOrUpdateDevice(macAddress, clientIp);
                    
                    writer.println("OK_CONNECTED");
                    log.info("✅ [TCP] Device connected: {} ({})", macAddress, clientIp);
                } else {
                    writer.println("ERROR_FORMAT");
                    log.warn("⚠️ [TCP] Malformed handshake from {}: {}", clientIp, firstLine);
                    return;
                }
            } else {
                log.warn("⚠️ [TCP] Invalid protocol from {}: {}", clientIp, firstLine);
                return;
            }

            // --- MESSAGE LOOP ---
            String line;
            while ((line = safeReadLine(inputStream)) != null) {
                deviceService.updateLastSeen(macAddress);
                line = line.trim();

                if (line.startsWith("{")) {
                    if (isAllowedToProcess(macAddress)) {
                        processJsonLog(macAddress, line, writer);
                    } else {
                        writer.println("ERROR_RATE_LIMIT");
                    }
                }
                else if (line.equals("PING")) {
                    writer.println("PONG");
                } 
                else if (line.equals("GET_ORDER")) {
                    processGetOrder(macAddress, writer);
                }
                else if (line.startsWith("STATUS:")) {
                    if (isAllowedToProcess(macAddress)) {
                        processStatusChange(macAddress, line, writer);
                    } else {
                        writer.println("ERROR_RATE_LIMIT");
                    }
                }
                else {
                    log.debug("❓ [TCP] Ignored command from {}: {}", macAddress, line);
                }
            }

        } catch (SocketTimeoutException e) {
            log.warn("⏰ [TCP] Timeout (no PING) for device: {}", macAddress);
        } catch (IOException e) {
            log.error("❌ [TCP] I/O error on connection with {}: {}", macAddress, e.getMessage());
        } catch (Exception e) {
            log.error("❌ [TCP] Unexpected error with {}: {}", macAddress, e.getMessage());
        } finally {
            cleanupConnection(macAddress, deviceSocket);
        }
    }

    // --- UTILITY AND SECURITY METHODS ---

    /**
     * Safe line reader that enforces a maximum length limit to prevent Out Of Memory attacks.
     */
    private String safeReadLine(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        int count = 0;
        
        while ((c = inputStream.read()) != -1) {
            if (c == '\n') break;
            if (c != '\r') {
                sb.append((char) c);
                count++;
            }
            if (count > MAX_LINE_LENGTH) {
                throw new IOException("Maximum line length exceeded (" + MAX_LINE_LENGTH + " chars)");
            }
        }
        
        if (c == -1 && sb.isEmpty()) {
            return null; // Stream end (disconnect)
        }
        return sb.toString();
    }

    /**
     * Basic rate limiter to throttle heavy processing tasks.
     */
    private boolean isAllowedToProcess(String macAddress) {
        long now = System.currentTimeMillis();
        long lastTime = lastActionTimes.getOrDefault(macAddress, 0L);
        
        if ((now - lastTime) < RATE_LIMIT_MS) {
            log.warn("🛑 [TCP] Rate limit hit for device {}", macAddress);
            return false;
        }
        
        lastActionTimes.put(macAddress, now);
        return true;
    }

    /**
     * Closes existing connections for a MAC address if a new one is initiated before the server detects a timeout.
     */
    private void killZombieConnection(String macAddress, Socket newSocket) {
        Socket existingSocket = activeConnections.get(macAddress);
        if (existingSocket != null && !existingSocket.isClosed() && existingSocket != newSocket) {
            log.warn("🧟 [TCP] Killing zombie connection for device: {}", macAddress);
            try {
                existingSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void cleanupConnection(String macAddress, Socket deviceSocket) {
        if (macAddress != null) {
            // Remove from map ONLY if the socket is the original one to prevent race conditions
            activeConnections.remove(macAddress, deviceSocket);
            lastActionTimes.remove(macAddress);
            
            log.info("🔌 [TCP] Device disconnected: {}", macAddress);
            deviceService.handleDisconnect(macAddress);
        }
        try {
            if (deviceSocket != null && !deviceSocket.isClosed()) {
                deviceSocket.close();
            }
        } catch (IOException ignored) {}
    }

    // --- BUSINESS LOGIC ---

    private void processJsonLog(String macAddress, String jsonLine, PrintWriter writer) {
        try {
            DeviceLogPayloadDTO payload = objectMapper.readValue(jsonLine, DeviceLogPayloadDTO.class);
            logService.registerLogFromDevice(macAddress, payload);
            log.info("📝 [TCP] Log Saved - Device: {}, OP: {}, Qty: {}", macAddress, payload.orderId(), payload.quantityProduced());
            writer.println("OK_LOG"); 
        } catch (JsonProcessingException e) {
            log.error("❌ [TCP] Invalid JSON from {}: {}", macAddress, jsonLine);
            writer.println("ERROR_JSON_FORMAT");
        } catch (Exception e) {
            log.error("❌ [TCP] Failed to save log from {}: {}", macAddress, e.getMessage());
            writer.println("ERROR_SAVING"); 
        }
    }

    private void processGetOrder(String macAddress, PrintWriter writer) {
        try {
            Order order = deviceService.getCurrentOrderEntityByMac(macAddress);
            if (order != null) {
                writer.println("ORDER:" + order.getId() + ":" + order.getCode());
                log.info("📤 [TCP] Sent ORDER:{}:{} to {}", order.getId(), order.getCode(), macAddress);
            } else {
                writer.println("NO_ORDER");
                log.info("📤 [TCP] Sent NO_ORDER to {}", macAddress);
            }
        } catch (Exception e) {
            log.error("Error fetching order for {}: {}", macAddress, e.getMessage());
            writer.println("ERROR_DB");
        }
    }

    private void processStatusChange(String macAddress, String line, PrintWriter writer) {
        try {
            String statusStr = line.split(":")[1].trim();
            ProcessStatus status = ProcessStatus.valueOf(statusStr);
            deviceService.updateProcessStatus(macAddress, status);
            writer.println("OK_STATUS");
        } catch (IllegalArgumentException e) {
            writer.println("ERROR_STATUS_INVALID");
        } catch (Exception e) {
            writer.println("ERROR_STATUS_PROCESS");
        }
    }

    // --- GRACEFUL SHUTDOWN ---

    @PreDestroy
    public void stop() {
        log.info("🛑 [TCP] Shutting down TCP server...");
        running = false;
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException ignored) {}

        activeConnections.values().forEach(socket -> {
            try {
                if (!socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
        });
        activeConnections.clear();

        executor.shutdown();
    }
}