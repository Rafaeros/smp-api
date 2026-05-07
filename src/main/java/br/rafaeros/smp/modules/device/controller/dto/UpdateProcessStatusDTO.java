package br.rafaeros.smp.modules.device.controller.dto;

import br.rafaeros.smp.modules.device.model.enums.ProcessStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProcessStatusDTO(
    @NotNull(message = "O status é obrigatório")
    ProcessStatus status
) {}
