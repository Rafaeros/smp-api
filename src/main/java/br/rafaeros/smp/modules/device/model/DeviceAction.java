package br.rafaeros.smp.modules.device.model;

import java.time.Instant;

import br.rafaeros.smp.core.model.BaseEntity;
import br.rafaeros.smp.modules.device.model.enums.DeviceActionStatus;
import br.rafaeros.smp.modules.device.model.enums.DeviceActionType;
import br.rafaeros.smp.modules.user.model.User;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "device_actions")
public class DeviceAction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceActionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceActionStatus status = DeviceActionStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id")
    private User triggeredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedBy;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant endedAt;

    // Stored at resolution time: Duration.between(startedAt, endedAt).getSeconds()
    private Long durationSeconds;
}
