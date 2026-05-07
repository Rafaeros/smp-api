package br.rafaeros.smp.modules.company.controller.dto;

import java.util.Set;

import br.rafaeros.smp.modules.company.model.enums.SectorAuthority;
import jakarta.validation.constraints.NotBlank;

public record CreateSectorDTO(
    @NotBlank(message = "O nome do setor é obrigatório")
    String name,

    Set<SectorAuthority> authorities
) {}
