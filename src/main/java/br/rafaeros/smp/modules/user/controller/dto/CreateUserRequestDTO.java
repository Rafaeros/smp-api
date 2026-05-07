package br.rafaeros.smp.modules.user.controller.dto;

import br.rafaeros.smp.core.validation.SafeString;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequestDTO(

    @NotBlank(message = "O nome é obrigatório")
    @SafeString
    String firstName,

    @NotBlank(message = "O sobrenome é obrigatório")
    @SafeString
    String lastName,

    @NotBlank(message = "O email é obrigatório")
    @Email
    String email,

    @NotBlank(message = "O nome de usuário é obrigatório")
    @Size(min = 3, max = 50, message = "O nome de usuário deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "O username deve conter apenas letras, números e underscore")
    String username,

    @NotBlank(message = "O Cargo é obrigatório")
    String role
) {}
