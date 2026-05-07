package br.rafaeros.smp.modules.device.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.rafaeros.smp.core.dto.ApiResponse;
import br.rafaeros.smp.modules.device.controller.dto.ActiveActionDTO;
import br.rafaeros.smp.modules.device.controller.dto.CreateDeviceRequestDTO;
import br.rafaeros.smp.modules.device.controller.dto.DeviceActionResponseDTO;
import br.rafaeros.smp.modules.device.controller.dto.DeviceDetailsResponseDTO;
import br.rafaeros.smp.modules.device.controller.dto.DeviceResponseDTO;
import br.rafaeros.smp.modules.device.controller.dto.ResolveActionDTO;
import br.rafaeros.smp.modules.device.controller.dto.TriggerActionDTO;
import br.rafaeros.smp.modules.device.controller.dto.UpdateDeviceDTO;
import br.rafaeros.smp.modules.device.controller.dto.UpdateProcessStatusDTO;
import br.rafaeros.smp.modules.device.service.DeviceActionService;
import br.rafaeros.smp.modules.device.service.DeviceService;
import br.rafaeros.smp.modules.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceActionService deviceActionService;

    // ── Device CRUD ───────────────────────────────────────────

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DeviceResponseDTO>>> getAllAvailableDevices() {
        return ResponseEntity.ok(ApiResponse.success("Dispositivos disponíveis listados com sucesso.",
                deviceService.findAllAvailableDevices()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceResponseDTO>> createDevice(@RequestBody @Valid CreateDeviceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dispositivo criado com sucesso!", deviceService.createDevice(dto)));
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

    @PatchMapping("/{id}")
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

    // ── Public device endpoints (called by IoT firmware via HTTP) ─

    @PatchMapping("/{mac}/status")
    public ResponseEntity<ApiResponse<Void>> updateDeviceStatus(@PathVariable String mac,
            @RequestBody @Valid UpdateProcessStatusDTO dto) {
        deviceService.updateProcessStatus(mac, dto.status());
        deviceService.updateLastSeen(mac);
        return ResponseEntity.ok(ApiResponse.success("Status atualizado.", null));
    }

    @PutMapping("/{mac}/heartbeat")
    public ResponseEntity<ApiResponse<Void>> heartbeat(@PathVariable String mac) {
        deviceService.updateLastSeen(mac);
        return ResponseEntity.ok(ApiResponse.success("PONG", null));
    }

    // ── Device Actions ────────────────────────────────────────

    @PostMapping("/{id}/actions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeviceActionResponseDTO>> triggerAction(
            @PathVariable Long id,
            @RequestBody @Valid TriggerActionDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ação acionada com sucesso!",
                        deviceActionService.triggerAction(id, dto.type(), user)));
    }

    @GetMapping("/{id}/actions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<DeviceActionResponseDTO>>> getActionHistory(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success("Histórico de ações listado.",
                deviceActionService.getActionHistory(id, pageable)));
    }

    @GetMapping("/{id}/actions/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ActiveActionDTO>>> getActiveActions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ações ativas listadas.",
                deviceActionService.getActiveActions(id)));
    }

    @PatchMapping("/{id}/actions/{actionId}/resolve")
    @PreAuthorize("hasAuthority('QUALITY_ACCESS') or hasAuthority('MAINTENANCE_ACCESS') or hasAuthority('PRODUCTION_ACCESS') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeviceActionResponseDTO>> resolveAction(
            @PathVariable Long id,
            @PathVariable Long actionId,
            @RequestBody(required = false) ResolveActionDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("Ação resolvida com sucesso!",
                deviceActionService.resolveAction(actionId, user, dto)));
    }
}
