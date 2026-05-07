package br.rafaeros.smp.modules.device.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.rafaeros.smp.core.exception.BusinessException;
import br.rafaeros.smp.core.exception.ResourceNotFoundException;
import br.rafaeros.smp.modules.company.model.enums.SectorAuthority;
import br.rafaeros.smp.modules.device.controller.dto.ActiveActionDTO;
import br.rafaeros.smp.modules.device.controller.dto.DeviceActionResponseDTO;
import br.rafaeros.smp.modules.device.controller.dto.ResolveActionDTO;
import br.rafaeros.smp.modules.device.model.Device;
import br.rafaeros.smp.modules.device.model.DeviceAction;
import br.rafaeros.smp.modules.device.model.DeviceActionReport;
import br.rafaeros.smp.modules.device.model.enums.DeviceActionStatus;
import br.rafaeros.smp.modules.device.model.enums.DeviceActionType;
import br.rafaeros.smp.modules.device.repository.DeviceActionRepository;
import br.rafaeros.smp.modules.device.repository.DeviceActionReportRepository;
import br.rafaeros.smp.modules.device.repository.DeviceRepository;
import br.rafaeros.smp.modules.user.model.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceActionService {

    private final DeviceActionRepository actionRepository;
    private final DeviceActionReportRepository reportRepository;
    private final DeviceRepository deviceRepository;

    @Transactional
    public DeviceActionResponseDTO triggerAction(Long deviceId, DeviceActionType type, User triggeredBy) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));

        if (actionRepository.existsByDeviceIdAndTypeAndStatus(deviceId, type, DeviceActionStatus.ACTIVE)) {
            throw new BusinessException("Já existe uma ação ativa do tipo " + type.name() + " para este dispositivo");
        }

        DeviceAction action = new DeviceAction();
        action.setDevice(device);
        action.setType(type);
        action.setStatus(DeviceActionStatus.ACTIVE);
        action.setTriggeredBy(triggeredBy);
        action.setStartedAt(Instant.now());

        return DeviceActionResponseDTO.fromEntity(actionRepository.save(action), null);
    }

    @Transactional
    public void triggerActionFromDevice(String macAddress, DeviceActionType type) {
        Device device = deviceRepository.findByMacAddress(macAddress)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado: " + macAddress));

        if (actionRepository.existsByDeviceIdAndTypeAndStatus(device.getId(), type, DeviceActionStatus.ACTIVE)) {
            throw new BusinessException("Ação duplicada: já existe ação ativa do tipo " + type.name());
        }

        DeviceAction action = new DeviceAction();
        action.setDevice(device);
        action.setType(type);
        action.setStatus(DeviceActionStatus.ACTIVE);
        action.setStartedAt(Instant.now());

        actionRepository.save(action);
    }

    @Transactional
    public DeviceActionResponseDTO resolveAction(Long actionId, User resolvedBy, ResolveActionDTO dto) {
        DeviceAction action = actionRepository.findById(actionId)
                .orElseThrow(() -> new ResourceNotFoundException("Ação não encontrada"));

        if (action.getStatus() == DeviceActionStatus.RESOLVED) {
            throw new BusinessException("Esta ação já foi resolvida");
        }

        validateResolverAuthority(resolvedBy, action.getType());

        Instant now = Instant.now();
        action.setStatus(DeviceActionStatus.RESOLVED);
        action.setResolvedBy(resolvedBy);
        action.setEndedAt(now);
        action.setDurationSeconds(Duration.between(action.getStartedAt(), now).getSeconds());

        actionRepository.save(action);

        DeviceActionReport report = null;
        if (action.getType() != DeviceActionType.BOBBIN_CHANGE) {
            report = new DeviceActionReport();
            report.setDeviceAction(action);
            report.setTechnician(resolvedBy);
            report.setReportNotes(dto != null ? dto.reportNotes() : null);
            report.setObservations(dto != null ? dto.observations() : null);
            report.setResolvedAt(now);
            report = reportRepository.save(report);
        }

        return DeviceActionResponseDTO.fromEntity(action, report);
    }

    @Transactional(readOnly = true)
    public List<ActiveActionDTO> getActiveActions(Long deviceId) {
        return actionRepository.findByDeviceIdAndStatus(deviceId, DeviceActionStatus.ACTIVE)
                .stream()
                .map(ActiveActionDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DeviceActionResponseDTO> getActionHistory(Long deviceId, Pageable pageable) {
        return actionRepository.findByDeviceIdOrderByStartedAtDesc(deviceId, pageable)
                .map(a -> DeviceActionResponseDTO.fromEntity(a, null));
    }

    private void validateResolverAuthority(User user, DeviceActionType type) {
        if (user.getSector() == null) return; // platform ADMIN can resolve anything

        boolean hasAuthority = switch (type) {
            case QUALITY -> user.getSector().getAuthorities().contains(SectorAuthority.QUALITY_ACCESS);
            case MAINTENANCE -> user.getSector().getAuthorities().contains(SectorAuthority.MAINTENANCE_ACCESS);
            case BOBBIN_CHANGE -> user.getSector().getAuthorities().contains(SectorAuthority.PRODUCTION_ACCESS);
        };

        if (!hasAuthority) {
            throw new BusinessException("Você não tem autoridade para resolver ações do tipo " + type.name());
        }
    }
}
