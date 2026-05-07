package br.rafaeros.smp.modules.device.controller.dto;

import java.time.Instant;

import br.rafaeros.smp.modules.device.model.DeviceActionReport;

public record DeviceActionReportDTO(
    Long id,
    String technicianUsername,
    String reportNotes,
    String observations,
    Instant resolvedAt
) {
    public static DeviceActionReportDTO fromEntity(DeviceActionReport report) {
        return new DeviceActionReportDTO(
            report.getId(),
            report.getTechnician() != null ? report.getTechnician().getUsername() : null,
            report.getReportNotes(),
            report.getObservations(),
            report.getResolvedAt()
        );
    }
}
