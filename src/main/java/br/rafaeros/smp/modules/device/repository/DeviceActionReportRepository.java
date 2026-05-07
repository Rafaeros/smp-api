package br.rafaeros.smp.modules.device.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import br.rafaeros.smp.modules.device.model.DeviceActionReport;

@Repository
public interface DeviceActionReportRepository extends JpaRepository<DeviceActionReport, Long> {
}
