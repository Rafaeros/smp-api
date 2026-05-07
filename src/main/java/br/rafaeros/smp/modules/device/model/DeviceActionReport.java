package br.rafaeros.smp.modules.device.model;

import java.time.Instant;

import br.rafaeros.smp.core.model.BaseEntity;
import br.rafaeros.smp.modules.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "device_action_reports")
public class DeviceActionReport extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_action_id", unique = true, nullable = false)
    private DeviceAction deviceAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_user_id")
    private User technician;

    @Column(columnDefinition = "TEXT")
    private String reportNotes;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(nullable = false)
    private Instant resolvedAt;
}
