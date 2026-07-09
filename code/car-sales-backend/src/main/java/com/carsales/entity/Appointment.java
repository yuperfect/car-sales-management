package com.carsales.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Integer appointmentId;

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

    @Column(name = "appointment_time", nullable = false)
    private LocalDateTime appointmentTime;

    @Column(name = "status", nullable = false, length = 10)
    private String status = "pending";

    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "remark", length = 500)
    private String remark;

    @PrePersist
    protected void onCreate() {
        this.createTime = LocalDateTime.now();
        if (this.status == null) {
            this.status = "pending";
        }
    }
}
