package com.carsales.repository;

import com.carsales.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {

    List<Appointment> findByCustomerId(Integer customerId);

    List<Appointment> findByStatus(String status);

    List<Appointment> findByCustomerIdAndStatus(Integer customerId, String status);

    boolean existsByCarId(Integer carId);

    @Query("SELECT a.carId, COUNT(a) as cnt FROM Appointment a GROUP BY a.carId ORDER BY cnt DESC")
    List<Object[]> countByCarIdGrouped();
}
