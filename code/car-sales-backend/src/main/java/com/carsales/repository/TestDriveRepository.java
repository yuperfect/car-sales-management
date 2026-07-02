package com.carsales.repository;

import com.carsales.entity.TestDrive;
import com.carsales.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestDriveRepository extends JpaRepository<TestDrive, Integer> {

    List<TestDrive> findByCustomerId(Integer customerId);

    List<TestDrive> findByStatus(OrderStatus status);

    List<TestDrive> findByCustomerIdAndStatus(Integer customerId, OrderStatus status);

    @Query("SELECT t.carId, COUNT(t) as cnt FROM TestDrive t GROUP BY t.carId ORDER BY cnt DESC")
    List<Object[]> countByCarIdGrouped();
}
