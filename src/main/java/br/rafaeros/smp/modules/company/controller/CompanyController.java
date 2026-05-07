package br.rafaeros.smp.modules.company.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.rafaeros.smp.core.dto.ApiResponse;
import br.rafaeros.smp.modules.company.controller.dto.CompanyResponseDTO;
import br.rafaeros.smp.modules.company.controller.dto.CreateCompanyDTO;
import br.rafaeros.smp.modules.company.controller.dto.CreateSectorDTO;
import br.rafaeros.smp.modules.company.controller.dto.SectorResponseDTO;
import br.rafaeros.smp.modules.company.controller.dto.UpdateCompanyDTO;
import br.rafaeros.smp.modules.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/companies")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    // ── Company CRUD ──────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> create(@RequestBody @Valid CreateCompanyDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Empresa criada com sucesso!", companyService.createCompany(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CompanyResponseDTO>>> findAll() {
        return ResponseEntity.ok(ApiResponse.success("Empresas listadas.", companyService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Empresa encontrada.", companyService.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponseDTO>> update(@PathVariable Long id,
            @RequestBody @Valid UpdateCompanyDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Empresa atualizada.", companyService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable Long id) {
        companyService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Empresa desativada com sucesso."));
    }

    // ── Sector CRUD ───────────────────────────────────────────

    @PostMapping("/{companyId}/sectors")
    public ResponseEntity<ApiResponse<SectorResponseDTO>> createSector(@PathVariable Long companyId,
            @RequestBody @Valid CreateSectorDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Setor criado com sucesso!",
                        companyService.createSector(companyId, dto)));
    }

    @GetMapping("/{companyId}/sectors")
    public ResponseEntity<ApiResponse<List<SectorResponseDTO>>> findSectors(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success("Setores listados.",
                companyService.findSectorsByCompany(companyId)));
    }

    @PatchMapping("/{companyId}/sectors/{sectorId}")
    public ResponseEntity<ApiResponse<SectorResponseDTO>> updateSector(@PathVariable Long companyId,
            @PathVariable Long sectorId, @RequestBody @Valid CreateSectorDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Setor atualizado.",
                companyService.updateSector(companyId, sectorId, dto)));
    }

    @DeleteMapping("/{companyId}/sectors/{sectorId}")
    public ResponseEntity<ApiResponse<Void>> deleteSector(@PathVariable Long companyId,
            @PathVariable Long sectorId) {
        companyService.deleteSector(companyId, sectorId);
        return ResponseEntity.ok(ApiResponse.success("Setor removido com sucesso."));
    }
}
