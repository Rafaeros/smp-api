package br.rafaeros.smp.modules.company.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.rafaeros.smp.modules.company.model.Sector;

@Repository
public interface SectorRepository extends JpaRepository<Sector, Long> {

    List<Sector> findByCompanyId(Long companyId);

    boolean existsByNameAndCompanyId(String name, Long companyId);
}
