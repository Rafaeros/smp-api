package br.rafaeros.smp.modules.auth.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import br.rafaeros.smp.core.exception.ResourceNotFoundException;
import br.rafaeros.smp.core.security.TokenService;
import br.rafaeros.smp.modules.auth.controller.dto.AuthRequestDTO;
import br.rafaeros.smp.modules.auth.controller.dto.AuthResponseDTO;
import br.rafaeros.smp.modules.auth.controller.dto.CompanyLookupResponseDTO;
import br.rafaeros.smp.modules.company.model.Company;
import br.rafaeros.smp.modules.company.repository.CompanyRepository;
import br.rafaeros.smp.modules.user.model.User;
import br.rafaeros.smp.modules.user.model.enums.Role;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final CompanyRepository companyRepository;

    public CompanyLookupResponseDTO lookupCompany(String slug) {
        Company company = companyRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada ou inativa"));
        return CompanyLookupResponseDTO.fromEntity(company);
    }

    public AuthResponseDTO login(AuthRequestDTO dto) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(dto.username(), dto.password());
            var authentication = authenticationManager.authenticate(usernamePassword);
            User user = (User) authentication.getPrincipal();

            // Platform ADMIN with no sector bypasses company validation
            boolean isPlatformAdmin = user.getRole() == Role.ADMIN && user.getSector() == null;

            if (!isPlatformAdmin) {
                if (dto.companySlug() == null || dto.companySlug().isBlank()) {
                    throw new BadCredentialsException("Credenciais inválidas");
                }
                Company company = companyRepository.findBySlugAndActiveTrue(dto.companySlug())
                        .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

                if (user.getSector() == null || !user.getSector().getCompany().getId().equals(company.getId())) {
                    throw new BadCredentialsException("Credenciais inválidas");
                }
            }

            String token = tokenService.generateToken(user);

            Set<String> authorities = user.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toSet());

            Long companyId = user.getSector() != null ? user.getSector().getCompany().getId() : null;
            String companyName = user.getSector() != null ? user.getSector().getCompany().getName() : null;
            Long sectorId = user.getSector() != null ? user.getSector().getId() : null;
            String sectorName = user.getSector() != null ? user.getSector().getName() : null;

            return new AuthResponseDTO(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getRole().name(),
                    companyId,
                    companyName,
                    sectorId,
                    sectorName,
                    authorities);

        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Credenciais inválidas");
        }
    }
}
