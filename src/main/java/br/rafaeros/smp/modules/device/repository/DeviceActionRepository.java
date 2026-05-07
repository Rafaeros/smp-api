package br.rafaeros.smp.modules.device.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.rafaeros.smp.modules.device.model.DeviceAction;
import br.rafaeros.smp.modules.device.model.enums.DeviceActionStatus;
import br.rafaeros.smp.modules.device.model.enums.DeviceActionType;

@Repository
public interface DeviceActionRepository extends JpaRepository<DeviceAction, Long> {

    List<DeviceAction> findByDeviceIdAndStatus(Long deviceId, DeviceActionStatus status);

    Page<DeviceAction> findByDeviceIdOrderByStartedAtDesc(Long deviceId, Pageable pageable);

    boolean existsByDeviceIdAndTypeAndStatus(Long deviceId, DeviceActionType type, DeviceActionStatus status);
}
