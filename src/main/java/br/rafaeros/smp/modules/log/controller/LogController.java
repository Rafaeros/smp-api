package br.rafaeros.smp.modules.log.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.rafaeros.smp.core.dto.ApiResponse;
import br.rafaeros.smp.modules.log.controller.dto.CreateLogRequestDTO;
import br.rafaeros.smp.modules.log.controller.dto.DeviceLogPayloadDTO;
import br.rafaeros.smp.modules.log.controller.dto.IOrderStats;
import br.rafaeros.smp.modules.log.controller.dto.LogResponseDTO;
import br.rafaeros.smp.modules.log.controller.dto.ProductStatsDTO;
import br.rafaeros.smp.modules.log.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LogResponseDTO>> create(@RequestBody @Valid CreateLogRequestDTO request) {
        return ResponseEntity.ok(ApiResponse.success("Log criado com sucesso.", logService.create(request)));
    }

    @PostMapping("/device")
    public ResponseEntity<ApiResponse<Void>> createFromDevice(@RequestBody @Valid DeviceLogPayloadDTO payload) {
        logService.registerLogFromDevice(payload.mac(), payload);
        
        return ResponseEntity.ok(ApiResponse.success("Log salvo pelo dispositivo com sucesso.", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LogResponseDTO>>> getAll(
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Logs listados com sucesso.",
                logService.findAll(pageable)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<Page<LogResponseDTO>>> getLogsByOrder(
            @PageableDefault(page = 0, size = 20) Pageable pageable, @PathVariable Long orderId) {
        return ResponseEntity.ok(
                ApiResponse.success("Logs listados com sucesso.", logService.findLogsByOrder(pageable, orderId)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<LogResponseDTO>>> getLogsByProduct(
            @PageableDefault(page = 0, size = 20) Pageable pageable, @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Logs listados com sucesso.",
                logService.findLogsByProduct(productId, pageable)));
    }

    @GetMapping("/stats/order/{orderId}")
    public ResponseEntity<ApiResponse<IOrderStats>> getOrderStats(@PathVariable Long orderId) {
        return ResponseEntity
                .ok(ApiResponse.success("Logs listados com sucesso.", logService.findOrderStats(orderId)));
    }

    @GetMapping("/stats/product/{productId}")
    public ResponseEntity<ApiResponse<ProductStatsDTO>> getProductStats(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Logs listados com sucesso.",
                logService.findProductStats(productId)));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportLogs() {

        byte[] fileBytes = logService.exportLogsToCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio_logs_producao.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(fileBytes);
    }
}
