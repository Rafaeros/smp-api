package br.rafaeros.smp.modules.order.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.rafaeros.smp.modules.order.controller.dto.OrderExportDTO;
import br.rafaeros.smp.modules.order.controller.dto.OrderSummaryDTO;
import br.rafaeros.smp.modules.order.model.Order;
import br.rafaeros.smp.modules.order.model.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
        Optional<Order> findByCode(String code);

        @Query("SELECT o FROM Order o " +
                        "LEFT JOIN o.product p " +
                        "LEFT JOIN o.client c " +
                        "WHERE " +
                        "(:code IS NULL OR LOWER(CAST(o.code AS string)) LIKE LOWER(CAST(:code AS string))) AND " +
                        "(:productCode IS NULL OR LOWER(CAST(p.code AS string)) LIKE LOWER(CAST(:productCode AS string))) AND "
                        +
                        "(:clientId IS NULL OR o.client.id = :clientId) AND " +
                        "(:status IS NULL OR o.status = :status) AND " +
                        "(o.deliveryDate >= :startDate) AND " +
                        "(o.deliveryDate <= :endDate)")
        Page<Order> findAllWithFilter(
                        Pageable pageable,
                        @Param("code") String code,
                        @Param("productCode") String productCode,
                        @Param("clientId") Long clientId,
                        @Param("status") OrderStatus status,
                        @Param("startDate") Instant startDate,
                        @Param("endDate") Instant endDate);

        @Query("""
                                SELECT new br.rafaeros.smp.modules.order.controller.dto.OrderSummaryDTO(
                                        o.id,
                                        o.code,
                                        p.code
                                )
                                FROM Order o
                                LEFT JOIN o.product p
                                WHERE (:search IS NULL OR LOWER(CAST(o.code AS string)) LIKE LOWER(CAST(:search AS string)))
                        """)
        Page<OrderSummaryDTO> findSummary(Pageable pageable, @Param("search") String search);

        @Query("""
                            SELECT new br.rafaeros.smp.modules.order.controller.dto.OrderExportDTO(
                                o.code,
                                p.code,
                                c.name,
                                CAST(o.status AS string),
                                o.totalQuantity,
                                o.producedQuantity,
                                COUNT(l.id),
                                MIN(l.cycleTime + COALESCE(l.pausedTime, 0.0)),
                                AVG(l.cycleTime + COALESCE(l.pausedTime, 0.0)),
                                MAX(l.cycleTime + COALESCE(l.pausedTime, 0.0))
                            )
                            FROM Order o
                            LEFT JOIN o.product p
                            LEFT JOIN o.client c
                            LEFT JOIN o.logs l
                            GROUP BY o.code, p.code, c.name, o.status, o.totalQuantity, o.producedQuantity
                        """)
        List<OrderExportDTO> getOrderExportStats();
}