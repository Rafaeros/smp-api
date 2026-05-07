package br.rafaeros.smp.modules.auth.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRequestDTO(
    @NotBlank(message = "O username é obrigatório")
    @Size(max = 50)
    String username,

    @NotBlank(message = "A senha é obrigatória")
    @Size(max = 100)
    String password,

    // Optional for platform ADMIN; required for company users
    String companySlug
) {}
