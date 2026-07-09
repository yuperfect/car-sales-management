package com.carsales.entity;

import com.carsales.enums.CarStatus;
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
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Integer carId;

    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "displacement", length = 20)
    private String displacement;

    @Column(name = "transmission", length = 20)
    private String transmission;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "listed_time", nullable = false, updatable = false)
    private LocalDateTime listedTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private CarStatus status = CarStatus.on_sale;

    @PrePersist
    protected void onCreate() {
        this.listedTime = LocalDateTime.now();
        if (this.stock == null) {
            this.stock = 0;
        }
        if (this.status == null) {
            this.status = CarStatus.on_sale;
        }
    }
}
