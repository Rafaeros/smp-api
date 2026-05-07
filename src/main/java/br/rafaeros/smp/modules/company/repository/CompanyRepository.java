package br.rafaeros.smp.modules.company.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.rafaeros.smp.modules.company.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findBySlug(String slug);

    Optional<Company> findBySlugAndActiveTrue(String slug);

    boolean existsBySlug(String slug);
}
