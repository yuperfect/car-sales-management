package com.carsales.repository;

import com.carsales.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByPhone(String phone);

    Optional<Customer> findByUsername(String username);

    @Query("SELECT c FROM Customer c WHERE c.realName = :realName AND c.username IS NULL ORDER BY c.firstSubmitTime ASC")
    List<Customer> findByRealNameAndUsernameIsNull(@Param("realName") String realName);

    List<Customer> findByUsernameContainingOrRealNameContainingOrPhoneContaining(
            String username, String realName, String phone);
}
