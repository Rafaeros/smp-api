package br.rafaeros.smp.modules.order.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.rafaeros.smp.core.dto.ApiResponse;
import br.rafaeros.smp.modules.device.service.DeviceService;
import br.rafaeros.smp.modules.order.controller.dto.CreateOrderDTO;
import br.rafaeros.smp.modules.order.controller.dto.OrderResponseDTO;
import br.rafaeros.smp.modules.order.controller.dto.OrderSummaryDTO;
import br.rafaeros.smp.modules.order.controller.dto.UpdateOrderDTO;
import br.rafaeros.smp.modules.order.model.Order;
import br.rafaeros.smp.modules.order.scraper.ErpSearchFilter;
import br.rafaeros.smp.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final DeviceService deviceService;

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<List<OrderResponseDTO>>> syncOrders(ErpSearchFilter filter,
            @RequestParam(defaultValue = "false") boolean force) {
        System.out.println("Filtro recebiodo: " + filter);
        return ResponseEntity
                .ok(ApiResponse.success("Ordens sincronizadas com sucesso!", orderService.syncFromErp(filter, force)));
    }

    @GetMapping("/device/{mac}/current")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getCurrentOrderByMac(@PathVariable String mac) {
        Order order = deviceService.getCurrentOrderEntityByMac(mac);
        
        if (order != null) {
            OrderResponseDTO dto = OrderResponseDTO.fromEntity(order);
            return ResponseEntity.ok(ApiResponse.success("Ordem encontrada.", dto));
        } else {
            return ResponseEntity.ok(ApiResponse.success("Nenhuma ordem ativa para este dispositivo.", null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponseDTO>> createOrder(@RequestBody CreateOrderDTO dto) {
        return ResponseEntity.ok(ApiResponse.success("Ordem criada com sucesso!", orderService.createOrder(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getAll(
            @PageableDefault(page = 0, size = 20) Pageable pageable, @ModelAttribute OrderSearchFilter filter) {
        return ResponseEntity
                .ok(ApiResponse.success("Ordens listadas com sucesso", orderService.findAll(pageable, filter)));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Page<OrderSummaryDTO>>> getSummary(
            @PageableDefault(page = 0, size = 20) Pageable pageable,
            @RequestParam(required = false) String code) {
        return ResponseEntity
                .ok(ApiResponse.success("Ordens listadas com sucesso", orderService.getSummary(pageable, code)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ordem encontrada.", orderService.findById(id)));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success("Ordem encontrada.", orderService.findByCode(code)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrder(@PathVariable Long id,
            @RequestBody UpdateOrderDTO dto) {
        return ResponseEntity
                .ok(ApiResponse.success("Ordem atualizada com sucesso!", orderService.updateOrder(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Ordem removida com sucesso!"));
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<byte[]> exportOrders() {

        byte[] fileBytes = orderService.exportOrdersToCsv();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio_ordens.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(fileBytes);
    }
}
