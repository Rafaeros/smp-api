package br.rafaeros.smp.modules.userdevice.service;

import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.rafaeros.smp.core.exception.BusinessException;
import br.rafaeros.smp.core.exception.ResourceNotFoundException;
import br.rafaeros.smp.modules.device.model.Device;
import br.rafaeros.smp.modules.device.model.enums.DeviceStatus;
import br.rafaeros.smp.modules.device.repository.DeviceRepository;
import br.rafaeros.smp.modules.device.service.DeviceActionService;
import br.rafaeros.smp.modules.order.model.Order;
import br.rafaeros.smp.modules.order.repository.OrderRepository;
import br.rafaeros.smp.modules.user.model.User;
import br.rafaeros.smp.modules.user.model.enums.Role;
import br.rafaeros.smp.modules.user.repository.UserRepository;
import br.rafaeros.smp.modules.userdevice.controller.dto.DeviceBindingDTO;
import br.rafaeros.smp.modules.userdevice.controller.dto.UpdateDeviceDetailsDTO;
import br.rafaeros.smp.modules.userdevice.controller.dto.UserDeviceDetailsDTO;
import br.rafaeros.smp.modules.userdevice.controller.dto.UserDeviceMapResponseDTO;
import br.rafaeros.smp.modules.userdevice.model.UserDevice;
import br.rafaeros.smp.modules.userdevice.repository.UserDeviceRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDeviceService {
    private final UserDeviceRepository userDeviceRepository;
    private final DeviceRepository deviceRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final DeviceActionService deviceActionService;

    @Transactional
    public UserDeviceDetailsDTO bindDeviceToUser(Long userId, DeviceBindingDTO dto) {
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        Device device = deviceRepository.findById(Objects.requireNonNull(dto.id()))
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));

        if (userDeviceRepository.existsByUserIdAndDeviceId(userId, dto.id())) {
            throw new BusinessException("Este dispositivo já está vinculado ao seu usuário.");
        }

        if (userDeviceRepository.existsByDeviceId(device.getId())) {
            throw new BusinessException("Este dispositivo já está vinculado a um usuário.");
        }

        UserDevice userDevice = new UserDevice();
        userDevice.setUser(user);
        userDevice.setDevice(device);
        userDevice.setName(dto.name());
        userDevice.setCoordinateX(dto.coordinateX());
        userDevice.setCoordinateY(dto.coordinateY());
        userDevice.getDevice().setCurrentOrder(null);

        UserDevice saved = userDeviceRepository.save(userDevice);
        return UserDeviceDetailsDTO.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<UserDeviceMapResponseDTO> getMyDevices(
            User user,
            String name,
            String macAddress,
            String status,
            Pageable pageable) {

        DeviceStatus deviceStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                deviceStatus = DeviceStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status inválido: " + status);
            }
        }
        String nameFilter = (name != null && !name.isBlank()) ? "%" + name.toLowerCase() + "%" : null;
        String macFilter = (macAddress != null && !macAddress.isBlank()) ? "%" + macAddress.toLowerCase() + "%" : null;

        Page<UserDevice> devicesPage;
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            devicesPage = userDeviceRepository.findAllWithFilters(
                    nameFilter,
                    macFilter,
                    deviceStatus,
                    pageable);
        } else {
            devicesPage = userDeviceRepository.findByUserIdAndFilters(
                    user.getId(),
                    nameFilter,
                    macFilter,
                    deviceStatus,
                    pageable);
        }
        return devicesPage.map(ud ->
                UserDeviceMapResponseDTO.fromEntity(ud,
                        deviceActionService.getActiveActions(ud.getDevice().getId())));
    }

    @Transactional(readOnly = true)
    public List<UserDeviceMapResponseDTO> getMyMap(User user) {
        List<UserDevice> devicesList;
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            devicesList = userDeviceRepository.findAllForMap();
        } else {
            devicesList = userDeviceRepository.findAllByUserIdForMap(user.getId());
        }
        return devicesList.stream()
                .map(ud -> UserDeviceMapResponseDTO.fromEntity(ud,
                        deviceActionService.getActiveActions(ud.getDevice().getId())))
                .toList();
    }

@Transactional(readOnly = true)
    public Page<UserDeviceDetailsDTO> findAllByUserId(Long userId, User user, Pageable pageable) {
        boolean isAdminOrManager = user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER;
        boolean isSelf = user.getId().equals(userId);

        if (isAdminOrManager || isSelf) {
            return userDeviceRepository.findAllByUserId(userId, pageable)
                    .map(UserDeviceDetailsDTO::fromEntity);
        } else {

            throw new BusinessException("Acesso negado: Você não tem permissão para visualizar estes dispositivos.");
        }
    }

    @Transactional(readOnly = true)
    public UserDeviceDetailsDTO findById(Long id, User user) {
        Long safeId = Objects.requireNonNull(id);
        UserDevice userDevice;
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            userDevice = userDeviceRepository.findById(safeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
        } else {
            userDevice = userDeviceRepository.findByIdAndUserId(id, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado ou acesso negado"));
        }

        return UserDeviceDetailsDTO.fromEntity(userDevice);
    }

    @Transactional
    public UserDeviceDetailsDTO updateDeviceDetails(Long userDeviceId, User user, UpdateDeviceDetailsDTO dto) {

        UserDevice userDevice;
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            userDevice = userDeviceRepository.findById(Objects.requireNonNull(userDeviceId))
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado"));
        } else {
            userDevice = userDeviceRepository.findByIdAndUserId(userDeviceId, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo não encontrado ou acesso negado"));
        }
        if (userDevice.getDevice() == null) {
            throw new ResourceNotFoundException("Dispositivo físico associado não encontrado");
        }
        if (dto.name() != null && !dto.name().isBlank()) {
            userDevice.setName(dto.name());
        }
        if (dto.processStage() != null) {
            userDevice.getDevice().setCurrentStage(dto.processStage());
        }
        if (dto.orderId() != null) {
            Long safeId = Objects.requireNonNull(dto.orderId());
            Order order = orderRepository.findById(safeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ordem de Produção não encontrada"));

            userDevice.getDevice().setCurrentOrder(order);
        }
        if (dto.coordinateX() != null) {
            userDevice.setCoordinateX(dto.coordinateX());
        }
        if (dto.coordinateY() != null) {
            userDevice.setCoordinateY(dto.coordinateY());
        }

        UserDevice updated = userDeviceRepository.save(userDevice);
        return UserDeviceDetailsDTO.fromEntity(updated);
    }

    @Transactional
    public void unbindDevice(Long userDeviceId, User user) {

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
            if (!userDeviceRepository.existsById(Objects.requireNonNull(userDeviceId))) {
                throw new ResourceNotFoundException("Dispositivo do usuário nao encontrado");
            }

            userDeviceRepository.deleteById(Objects.requireNonNull(userDeviceId));
            return;
        }

        UserDevice userDevice = userDeviceRepository.findByIdAndUserId(userDeviceId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo do usuário não encontrado"));

        if (userDevice.getDevice() == null) {
            throw new ResourceNotFoundException("Dispositivo associado não encontrado");
        }

        userDeviceRepository.delete(userDevice);
    }
}