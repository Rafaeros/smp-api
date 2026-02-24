package br.rafaeros.smp.modules.order.controller.dto;

import br.rafaeros.smp.modules.order.model.Order;

public record OrderSummaryDTO(
        Long id,
        String code,
        String productCode
    ) {
    public static OrderSummaryDTO fromEntity(Order order) {
        return new OrderSummaryDTO(order.getId(), order.getCode(), order.getProduct().getCode());
    }
}
