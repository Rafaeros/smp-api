package br.rafaeros.smp.modules.device.controller.dto;

import java.time.Instant;

import br.rafaeros.smp.modules.device.model.DeviceAction;

public record ActiveActionDTO(
    Long id,
    String type,
    Instant startedAt
) {
    public static ActiveActionDTO fromEntity(DeviceAction action) {
        return new ActiveActionDTO(action.getId(), action.getType().name(), action.getStartedAt());
    }
}
