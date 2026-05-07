package br.rafaeros.smp.modules.device.controller.dto;

import br.rafaeros.smp.modules.device.model.enums.DeviceActionType;
import jakarta.validation.constraints.NotNull;

public record TriggerActionDTO(
    @NotNull(message = "O tipo de ação é obrigatório")
    DeviceActionType type
) {}
