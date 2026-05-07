package br.rafaeros.smp.modules.device.controller.dto;

import java.time.Instant;

import br.rafaeros.smp.modules.device.model.DeviceAction;
import br.rafaeros.smp.modules.device.model.DeviceActionReport;

public record DeviceActionResponseDTO(
    Long id,
    String type,
    String status,
    String triggeredByUsername,
    String resolvedByUsername,
    Instant startedAt,
    Instant endedAt,
    Long durationSeconds,
    DeviceActionReportDTO report
) {
    public static DeviceActionResponseDTO fromEntity(DeviceAction action, DeviceActionReport report) {
        return new DeviceActionResponseDTO(
            action.getId(),
            action.getType().name(),
            action.getStatus().name(),
            action.getTriggeredBy() != null ? action.getTriggeredBy().getUsername() : null,
            action.getResolvedBy() != null ? action.getResolvedBy().getUsername() : null,
            action.getStartedAt(),
            action.getEndedAt(),
            action.getDurationSeconds(),
            report != null ? DeviceActionReportDTO.fromEntity(report) : null
        );
    }
}
