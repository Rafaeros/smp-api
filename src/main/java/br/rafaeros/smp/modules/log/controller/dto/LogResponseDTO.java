package br.rafaeros.smp.modules.log.controller.dto;

import java.time.Instant;

import br.rafaeros.smp.modules.device.controller.dto.DeviceSummaryDTO;
import br.rafaeros.smp.modules.device.model.enums.ProcessStage;
import br.rafaeros.smp.modules.log.model.Log;
import br.rafaeros.smp.modules.order.controller.dto.OrderSummaryDTO;

public record LogResponseDTO(
    Long id,
    Instant createdAt,
    Double cycleTime,
    Double pausedTime,
    Double totalTime,
    Long quantityProduced,
    Long quantityPaused,
    ProcessStage processStage,
    DeviceSummaryDTO device,
    OrderSummaryDTO order
) {

    public static LogResponseDTO fromEntity(Log log) {
        DeviceSummaryDTO deviceDto = null;
        if (log.getDevice() != null) {
            deviceDto = new DeviceSummaryDTO(
                log.getDevice().getId(), 
                log.getDevice().getMacAddress()
            );
        }
        OrderSummaryDTO orderDto = null;

        if (log.getOrder() != null) {
            orderDto = new OrderSummaryDTO(
                log.getOrder().getId(), 
                log.getOrder().getCode(),
                log.getOrder().getProduct().getCode()
            );
        }

        return new LogResponseDTO(
            log.getId(),
            log.getCreatedAt(),
            log.getCycleTime(),
            log.getPausedTime(),
            log.getTotalTime(),
            log.getQuantityProduced(),
            log.getQuantityPaused(),
            log.getStage(),
            deviceDto,
            orderDto
        );
    }
}