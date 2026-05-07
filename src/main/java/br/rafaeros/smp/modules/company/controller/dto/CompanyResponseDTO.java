package br.rafaeros.smp.modules.company.controller.dto;

import br.rafaeros.smp.modules.company.model.Company;

public record CompanyResponseDTO(
    Long id,
    String name,
    String slug,
    boolean active,
    String logoUrl
) {
    public static CompanyResponseDTO fromEntity(Company company) {
        return new CompanyResponseDTO(
            company.getId(),
            company.getName(),
            company.getSlug(),
            company.isActive(),
            company.getLogoUrl()
        );
    }
}
