package br.rafaeros.smp.modules.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyLookupRequestDTO(
    @NotBlank(message = "O slug da empresa é obrigatório")
    String slug
) {}
