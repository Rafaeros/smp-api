package br.rafaeros.smp.modules.device.model;

import java.time.Instant;

import br.rafaeros.smp.core.model.BaseEntity;
import br.rafaeros.smp.modules.company.model.Company;
import br.rafaeros.smp.modules.device.model.enums.DeviceStatus;
import br.rafaeros.smp.modules.device.model.enums.ProcessStage;
import br.rafaeros.smp.modules.device.model.enums.ProcessStatus;
import br.rafaeros.smp.modules.order.model.Order;
import jakarta.persistence.Column;
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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "devices")
public class Device extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String macAddress;

    private String ipAddress;

    @Enumerated(EnumType.STRING)
    private DeviceStatus status;

    @Enumerated(EnumType.STRING)
    private ProcessStage currentStage = ProcessStage.PANEL_ASSEMBLY;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_order_id")
    private Order currentOrder;

    @Enumerated(EnumType.STRING)
    private ProcessStatus processStatus;

    private Instant lastSeen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;
}
