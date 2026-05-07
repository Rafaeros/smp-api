package br.rafaeros.smp.modules.user.controller.dto;

import br.rafaeros.smp.modules.user.model.User;

public record UserResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String username,
        String email,
        String role,
        Long sectorId,
        String sectorName,
        Long companyId,
        String companyName
    ) {
    public static UserResponseDTO fromEntity(User user) {
        Long sectorId = user.getSector() != null ? user.getSector().getId() : null;
        String sectorName = user.getSector() != null ? user.getSector().getName() : null;
        Long companyId = user.getSector() != null ? user.getSector().getCompany().getId() : null;
        String companyName = user.getSector() != null ? user.getSector().getCompany().getName() : null;

        return new UserResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                sectorId,
                sectorName,
                companyId,
                companyName);
    }
}
