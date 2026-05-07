package br.rafaeros.smp.modules.auth.controller.dto;

import java.util.Set;

public record AuthResponseDTO(
    String token,
    Long id,
    String username,
    String role,
    Long companyId,
    String companyName,
    Long sectorId,
    String sectorName,
    Set<String> authorities
) {}
