package br.rafaeros.smp.modules.company.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.rafaeros.smp.core.exception.BusinessException;
import br.rafaeros.smp.core.exception.ResourceNotFoundException;
import br.rafaeros.smp.modules.company.controller.dto.CompanyResponseDTO;
import br.rafaeros.smp.modules.company.controller.dto.CreateCompanyDTO;
import br.rafaeros.smp.modules.company.controller.dto.CreateSectorDTO;
import br.rafaeros.smp.modules.company.controller.dto.SectorResponseDTO;
import br.rafaeros.smp.modules.company.controller.dto.UpdateCompanyDTO;
import br.rafaeros.smp.modules.company.model.Company;
import br.rafaeros.smp.modules.company.model.Sector;
import br.rafaeros.smp.modules.company.repository.CompanyRepository;
import br.rafaeros.smp.modules.company.repository.SectorRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SectorRepository sectorRepository;

    @Transactional
    public CompanyResponseDTO createCompany(CreateCompanyDTO dto) {
        if (companyRepository.existsBySlug(dto.slug())) {
            throw new BusinessException("Já existe uma empresa com o slug '" + dto.slug() + "'");
        }
        Company company = new Company();
        company.setName(dto.name());
        company.setSlug(dto.slug());
        company.setLogoUrl(dto.logoUrl());
        return CompanyResponseDTO.fromEntity(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponseDTO> findAll() {
        return companyRepository.findAll().stream().map(CompanyResponseDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponseDTO findById(Long id) {
        return CompanyResponseDTO.fromEntity(
                companyRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada")));
    }

    @Transactional
    public CompanyResponseDTO update(Long id, UpdateCompanyDTO dto) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        if (dto.name() != null && !dto.name().isBlank()) company.setName(dto.name());
        if (dto.logoUrl() != null) company.setLogoUrl(dto.logoUrl());
        if (dto.active() != null) company.setActive(dto.active());
        return CompanyResponseDTO.fromEntity(companyRepository.save(company));
    }

    @Transactional
    public void deactivate(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        company.setActive(false);
        companyRepository.save(company);
    }

    // ── Sectors ───────────────────────────────────────────────

    @Transactional
    public SectorResponseDTO createSector(Long companyId, CreateSectorDTO dto) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada"));
        if (sectorRepository.existsByNameAndCompanyId(dto.name(), companyId)) {
            throw new BusinessException("Já existe um setor com o nome '" + dto.name() + "' nesta empresa");
        }
        Sector sector = new Sector();
        sector.setName(dto.name());
        sector.setCompany(company);
        if (dto.authorities() != null) sector.setAuthorities(dto.authorities());
        return SectorResponseDTO.fromEntity(sectorRepository.save(sector));
    }

    @Transactional(readOnly = true)
    public List<SectorResponseDTO> findSectorsByCompany(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ResourceNotFoundException("Empresa não encontrada");
        }
        return sectorRepository.findByCompanyId(companyId).stream()
                .map(SectorResponseDTO::fromEntity).toList();
    }

    @Transactional
    public SectorResponseDTO updateSector(Long companyId, Long sectorId, CreateSectorDTO dto) {
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado"));
        if (!sector.getCompany().getId().equals(companyId)) {
            throw new BusinessException("Setor não pertence a esta empresa");
        }
        if (dto.name() != null && !dto.name().isBlank()) sector.setName(dto.name());
        if (dto.authorities() != null) sector.setAuthorities(dto.authorities());
        return SectorResponseDTO.fromEntity(sectorRepository.save(sector));
    }

    @Transactional
    public void deleteSector(Long companyId, Long sectorId) {
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Setor não encontrado"));
        if (!sector.getCompany().getId().equals(companyId)) {
            throw new BusinessException("Setor não pertence a esta empresa");
        }
        sectorRepository.delete(sector);
    }
}
