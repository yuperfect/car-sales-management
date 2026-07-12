package com.carsales.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Column(name = "username", length = 50)
    private String username;

    @JsonIgnore
    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "real_name", nullable = false, length = 50)
    private String realName;

    @Column(name = "phone", nullable = false, length = 11)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "gender", length = 10)
    private String gender = "保密";

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "first_submit_time", nullable = false, updatable = false)
    private LocalDateTime firstSubmitTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        if (this.firstSubmitTime == null) {
            this.firstSubmitTime = LocalDateTime.now();
        }
        if (this.gender == null) {
            this.gender = "保密";
        }
    }
}
