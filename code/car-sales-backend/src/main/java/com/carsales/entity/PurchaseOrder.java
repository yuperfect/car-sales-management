package com.carsales.entity;

import com.carsales.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    @Column(name = "customer_id", nullable = false, insertable = false, updatable = false)
    private Integer customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Customer customer;

    @Column(name = "car_id", nullable = false, insertable = false, updatable = false)
    private Integer carId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Car car;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "order_time", nullable = false, updatable = false)
    private LocalDateTime orderTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private OrderStatus status = OrderStatus.pending;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "handler", length = 50)
    private String handler;

    @PrePersist
    protected void onCreate() {
        this.orderTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.pending;
        }
    }

    @JsonProperty("customerName")
    public String getCustomerName() {
        return customer != null ? customer.getRealName() : null;
    }

    @JsonProperty("customerPhone")
    public String getCustomerPhone() {
        return customer != null ? customer.getPhone() : null;
    }

    @JsonProperty("carBrand")
    public String getCarBrand() {
        return car != null ? car.getBrand() : null;
    }

    @JsonProperty("carModel")
    public String getCarModel() {
        return car != null ? car.getModel() : null;
    }
}
