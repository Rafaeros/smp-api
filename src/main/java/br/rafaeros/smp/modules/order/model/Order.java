package br.rafaeros.smp.modules.order.model;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.rafaeros.smp.core.model.BaseEntity;
import br.rafaeros.smp.modules.client.model.Client;
import br.rafaeros.smp.modules.company.model.Company;
import br.rafaeros.smp.modules.device.model.Device;
import br.rafaeros.smp.modules.log.model.Log;
import br.rafaeros.smp.modules.order.model.enums.OrderStatus;
import br.rafaeros.smp.modules.product.model.Product;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Instant creationDate;
    private Instant deliveryDate;

    private Integer totalQuantity;
    private Integer producedQuantity;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = true)
    private Company company;

    @JsonIgnore
    @OneToMany(mappedBy = "currentOrder", fetch = FetchType.LAZY)
    private List<Device> activeDevices;

    @JsonIgnore
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<Log> logs;
}
