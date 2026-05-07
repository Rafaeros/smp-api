package br.rafaeros.smp.modules.company.model;

import java.util.HashSet;
import java.util.Set;

import br.rafaeros.smp.core.model.BaseEntity;
import br.rafaeros.smp.modules.company.model.enums.SectorAuthority;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sectors")
public class Sector extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sector_authorities", joinColumns = @JoinColumn(name = "sector_id"))
    @Column(name = "authority")
    @Enumerated(EnumType.STRING)
    private Set<SectorAuthority> authorities = new HashSet<>();
}
