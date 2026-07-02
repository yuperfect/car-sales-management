package com.carsales.repository;

import com.carsales.entity.Car;
import com.carsales.enums.CarStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Integer> {

    List<Car> findByStatus(CarStatus status);

    List<Car> findByCategoryId(Integer categoryId);

    List<Car> findByBrandContaining(String brand);

    List<Car> findByModelContaining(String model);

    List<Car> findByCategoryIdAndStatus(Integer categoryId, CarStatus status);

    @Query("SELECT c FROM Car c WHERE (:categoryId IS NULL OR c.categoryId = :categoryId) " +
           "AND (:brand IS NULL OR c.brand LIKE %:brand%) " +
           "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR c.price <= :maxPrice)")
    List<Car> findByFilters(@Param("categoryId") Integer categoryId,
                            @Param("brand") String brand,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice);
}
