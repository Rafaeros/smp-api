package br.rafaeros.smp.modules.userdevice.controller.dto;

import br.rafaeros.smp.modules.device.model.enums.DeviceStatus;
import br.rafaeros.smp.modules.device.model.enums.ProcessStage;
import br.rafaeros.smp.modules.device.model.enums.ProcessStatus;
import br.rafaeros.smp.modules.userdevice.model.UserDevice;

public record UserDeviceDetailsDTO(
        Long id,
        String name,
        String macAddress,
        String ipAddress,
        DeviceStatus status,
        ProcessStatus process,
        ProcessStage stage,
        String code,
        String productCode,
        String lastSeen,
        Double coordinateX,
        Double coordinateY,
        String createdAt,
        String updatedAt) {
    public static UserDeviceDetailsDTO fromEntity(UserDevice userDevice) {
        return new UserDeviceDetailsDTO(
                userDevice.getId(),
                userDevice.getName(),
                userDevice.getDevice().getMacAddress(),
                userDevice.getDevice().getIpAddress(),
                userDevice.getDevice().getStatus(),
                userDevice.getDevice().getProcessStatus(),
                userDevice.getDevice().getCurrentStage(),
                (userDevice.getDevice().getCurrentOrder() != null) ? userDevice.getDevice().getCurrentOrder().getCode() : "",
                (userDevice.getDevice().getCurrentOrder() != null) ? userDevice.getDevice().getCurrentOrder().getProduct().getCode() : "",
                userDevice.getDevice().getLastSeen().toString(),
                userDevice.getCoordinateX(),
                userDevice.getCoordinateY(),
                userDevice.getDevice().getCreatedAt().toString(),
                userDevice.getDevice().getUpdatedAt().toString());
    }
}
