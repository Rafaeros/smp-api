package br.rafaeros.smp.modules.device.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.rafaeros.smp.core.dto.ApiResponse;
import br.rafaeros.smp.modules.device.controller.dto.CreateDeviceRequestDTO;
import br.rafaeros.smp.modules.device.controller.dto.DeviceDetailsResponseDTO;
import br.rafaeros.smp.modules.device.controller.dto.DeviceResponseDTO;
import br.rafaeros.smp.modules.device.controller.dto.UpdateDeviceDTO;
import br.rafaeros.smp.modules.device.model.enums.ProcessStatus;
import br.rafaeros.smp.modules.device.service.DeviceService;
import br.rafaeros.smp.modules.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DeviceResponseDTO>>> getAllAvailableDevices() {
        return ResponseEntity.ok(ApiResponse.success("Dispositivos disponíveis listados com sucesso.",
                deviceService.findAllAvailableDevices()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> createDevice(@RequestBody @Valid CreateDeviceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispositivo criado com sucesso!", deviceService.createDevice(dto)));
    }

    @PostMapping("/{mac}/status")
    public ResponseEntity<ApiResponse<Void>> updateDeviceStatus(@PathVariable String mac, @RequestBody Map<String, String> body) {
        try {
            ProcessStatus status = ProcessStatus.valueOf(body.get("status"));
            deviceService.updateProcessStatus(mac, status);
            deviceService.updateLastSeen(mac);
            return ResponseEntity.ok(ApiResponse.success("Status atualizado.", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{mac}/ping")
    public ResponseEntity<ApiResponse<Void>> pingDevice(@PathVariable String mac) {
        deviceService.updateLastSeen(mac);
        return ResponseEntity.ok(ApiResponse.success("PONG", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DeviceResponseDTO>>> getAllDevices() {
        return ResponseEntity.ok(ApiResponse.success("Dispositivos listados com sucesso.", deviceService.findAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@deviceSecurity.canAccessDevice(#id, principal)")
    public ResponseEntity<ApiResponse<DeviceDetailsResponseDTO>> getDeviceById(@PathVariable Long id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Dispositivo encontrado.", deviceService.findById(id)));
    }

    @PatchMapping("/update/{id}")
    @PreAuthorize("@deviceSecurity.canAccessDevice(#id, principal)")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> updateDeviceById(@PathVariable Long id,
            @RequestBody @Valid UpdateDeviceDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity
                .ok(ApiResponse.success("Dispositivo atualizado com sucesso!", deviceService.updateById(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDeviceById(@PathVariable Long id) {
        deviceService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Dispositivo removido com sucesso!"));
    }

}
