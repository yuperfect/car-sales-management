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

    List<Car> findByBrandContaining(String brand);

    List<Car> findByModelContaining(String model);

    @Query("SELECT c FROM Car c WHERE " +
           "(:brand IS NULL OR c.brand LIKE %:brand%) " +
           "AND (:model IS NULL OR c.model LIKE %:model%) " +
            "AND (:minPrice IS NULL OR c.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR c.price <= :maxPrice)")
    List<Car> findByFilters(@Param("brand") String brand,
                            @Param("model") String model,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT c FROM Car c WHERE c.brand LIKE %:keyword% OR c.model LIKE %:keyword%")
    List<Car> findByKeyword(@Param("keyword") String keyword);
}
