package br.rafaeros.smp.modules.company.controller.dto;

import jakarta.validation.constraints.NotNull;

public record AssignSectorDTO(
    @NotNull(message = "O ID do setor é obrigatório")
    Long sectorId
) {}
