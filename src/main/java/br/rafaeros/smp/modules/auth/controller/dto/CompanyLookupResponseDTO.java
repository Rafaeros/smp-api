package br.rafaeros.smp.modules.auth.controller.dto;

import br.rafaeros.smp.modules.company.model.Company;

public record CompanyLookupResponseDTO(
    Long id,
    String name,
    String slug,
    String logoUrl
) {
    public static CompanyLookupResponseDTO fromEntity(Company company) {
        return new CompanyLookupResponseDTO(
            company.getId(),
            company.getName(),
            company.getSlug(),
            company.getLogoUrl()
        );
    }
}
