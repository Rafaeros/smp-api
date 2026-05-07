package br.rafaeros.smp.modules.userdevice.controller.dto;

import java.util.List;

import br.rafaeros.smp.modules.device.controller.dto.ActiveActionDTO;
import br.rafaeros.smp.modules.device.model.enums.DeviceStatus;
import br.rafaeros.smp.modules.device.model.enums.ProcessStatus;
import br.rafaeros.smp.modules.userdevice.model.UserDevice;

public record UserDeviceMapResponseDTO(
        Long id,
        String name,
        String macAddress,
        Double x,
        Double y,
        DeviceStatus status,
        ProcessStatus process,
        List<ActiveActionDTO> activeActions
    ) {
    public static UserDeviceMapResponseDTO fromEntity(UserDevice userDevice, List<ActiveActionDTO> activeActions) {
        return new UserDeviceMapResponseDTO(
                userDevice.getId(),
                userDevice.getName(),
                userDevice.getDevice().getMacAddress(),
                userDevice.getCoordinateX(),
                userDevice.getCoordinateY(),
                userDevice.getDevice().getStatus(),
                userDevice.getDevice().getProcessStatus(),
                activeActions
            );
    }
}
