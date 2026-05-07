package br.rafaeros.smp.modules.company.controller.dto;

import java.util.Set;
import java.util.stream.Collectors;

import br.rafaeros.smp.modules.company.model.Sector;

public record SectorResponseDTO(
    Long id,
    String name,
    Long companyId,
    String companyName,
    Set<String> authorities
) {
    public static SectorResponseDTO fromEntity(Sector sector) {
        return new SectorResponseDTO(
            sector.getId(),
            sector.getName(),
            sector.getCompany().getId(),
            sector.getCompany().getName(),
            sector.getAuthorities().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }
}
