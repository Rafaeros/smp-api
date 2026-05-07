package br.rafaeros.smp.modules.user.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.rafaeros.smp.core.dto.ApiResponse;
import br.rafaeros.smp.modules.company.controller.dto.AssignSectorDTO;
import br.rafaeros.smp.modules.user.controller.dto.CreateUserRequestDTO;
import br.rafaeros.smp.modules.user.controller.dto.UpdatePasswordRequestDTO;
import br.rafaeros.smp.modules.user.controller.dto.UpdateUserRequestDTO;
import br.rafaeros.smp.modules.user.controller.dto.UserResponseDTO;
import br.rafaeros.smp.modules.user.model.User;
import br.rafaeros.smp.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> createUser(@RequestBody @Valid CreateUserRequestDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Usuário criado com sucesso!", userService.createUser(dto)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAll(
            @PageableDefault(page = 0, size = 10) Pageable pageable, @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName, @RequestParam(required = false) String username,
            @RequestParam(required = false) String email) {
        return ResponseEntity.ok(ApiResponse.success("Usuários listados com sucesso.",
                userService.findAll(pageable, firstName, lastName, username, email)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #id == principal.id")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getUserById(@PathVariable Long id,
            @AuthenticationPrincipal User User) {
        return ResponseEntity.ok(ApiResponse.success("Usuário encontrado.", userService.findById(id)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #id == principal.id")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(@PathVariable Long id,
            @RequestBody @Valid UpdateUserRequestDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity
                .ok(ApiResponse.success("Dados pessoais atualizados com sucesso!", userService.update(id, dto, user)));
    }

    @PatchMapping("/{id}/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') or #id == principal.id")
    public ResponseEntity<ApiResponse<UserResponseDTO>> changePassword(@PathVariable Long id,
            @RequestBody @Valid UpdatePasswordRequestDTO dto, @AuthenticationPrincipal User user) {
        return ResponseEntity
                .ok(ApiResponse.success("Senha atualizada com sucesso!", userService.changePassword(id, dto, user)));
    }

    @PatchMapping("/{id}/sector")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponseDTO>> assignSector(@PathVariable Long id,
            @RequestBody @Valid AssignSectorDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Setor atribuído com sucesso!",
                userService.assignSector(id, dto.sectorId())));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Usuário removido com sucesso!"));
    }

}
