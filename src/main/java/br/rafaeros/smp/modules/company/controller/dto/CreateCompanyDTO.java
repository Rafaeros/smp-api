package br.rafaeros.smp.modules.company.controller.dto;

import br.rafaeros.smp.core.validation.SafeString;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCompanyDTO(
    @NotBlank(message = "O nome da empresa é obrigatório")
    @SafeString
    String name,

    @NotBlank(message = "O slug da empresa é obrigatório")
    @Size(max = 100)
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug deve conter apenas letras minúsculas, números e hífens")
    String slug,

    String logoUrl
) {}
