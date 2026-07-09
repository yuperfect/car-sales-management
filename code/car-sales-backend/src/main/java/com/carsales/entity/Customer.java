package com.carsales.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "customer")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "phone", nullable = false, length = 11)
    private String phone;

    @Column(name = "first_submit_time", nullable = false, updatable = false)
    private LocalDateTime firstSubmitTime;

    @PrePersist
    protected void onCreate() {
        if (this.firstSubmitTime == null) {
            this.firstSubmitTime = LocalDateTime.now();
        }
    }
}
