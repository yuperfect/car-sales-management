package com.carsales.repository;

import com.carsales.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByUsername(String username);

    List<Customer> findByUsernameContainingOrRealNameContainingOrPhoneContaining(
            String username, String realName, String phone);
}
