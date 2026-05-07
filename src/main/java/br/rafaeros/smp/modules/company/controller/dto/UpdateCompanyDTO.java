package br.rafaeros.smp.modules.company.controller.dto;

import br.rafaeros.smp.core.validation.SafeString;

public record UpdateCompanyDTO(
    @SafeString
    String name,
    String logoUrl,
    Boolean active
) {}
