package com.carsales.repository;

import com.carsales.entity.PurchaseOrder;
import com.carsales.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Integer> {

    List<PurchaseOrder> findByCustomerId(Integer customerId);

    List<PurchaseOrder> findByStatus(OrderStatus status);

    List<PurchaseOrder> findByCustomerIdAndStatus(Integer customerId, OrderStatus status);

    @Query("SELECT o.carId, SUM(o.quantity) as totalQty FROM PurchaseOrder o " +
           "WHERE o.status = 'confirmed' GROUP BY o.carId ORDER BY totalQty DESC")
    List<Object[]> countSalesByCarIdGrouped();

    // 销售额占比统计：按车型分组计算总销售额
    @Query("SELECT o.carId, SUM(o.totalPrice) as totalRevenue FROM PurchaseOrder o " +
           "WHERE o.status = 'confirmed' GROUP BY o.carId ORDER BY totalRevenue DESC")
    List<Object[]> sumRevenueByCarIdGrouped();

    @Query("SELECT o FROM PurchaseOrder o WHERE " +
           "(:carType IS NULL OR o.car.model LIKE %:carType% OR o.car.brand LIKE %:carType%) " +
           "AND (:minPrice IS NULL OR o.totalPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR o.totalPrice <= :maxPrice) " +
           "AND (:customerName IS NULL OR o.customer.realName LIKE %:customerName%) " +
           "AND (:startDate IS NULL OR o.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR o.createdAt <= :endDate)")
    List<PurchaseOrder> findByFilters(@Param("carType") String carType,
                                       @Param("minPrice") BigDecimal minPrice,
                                       @Param("maxPrice") BigDecimal maxPrice,
                                       @Param("customerName") String customerName,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
