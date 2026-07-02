package com.carsales.service;

import com.carsales.entity.Car;
import com.carsales.entity.PurchaseOrder;
import com.carsales.repository.CarRepository;
import com.carsales.repository.TestDriveRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final TestDriveService testDriveService;
    private final OrderService orderService;
    private final CarRepository carRepository;

    public StatisticsService(TestDriveService testDriveService,
                             OrderService orderService,
                             CarRepository carRepository) {
        this.testDriveService = testDriveService;
        this.orderService = orderService;
        this.carRepository = carRepository;
    }

    /**
     * 试驾热点统计：按车型分组统计预约次数
     */
    public List<Map<String, Object>> getTestDriveHotStats() {
        List<Object[]> raw = testDriveService.getTestDriveHotStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Integer carId = (Integer) row[0];
            Long count = (Long) row[1];
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("carId", carId);
            item.put("count", count);
            carRepository.findById(carId).ifPresent(car -> {
                item.put("brand", car.getBrand());
                item.put("model", car.getModel());
            });
            result.add(item);
        }
        return result;
    }

    /**
     * 销量热点统计：按车型分组统计销量
     */
    public List<Map<String, Object>> getSalesHotStats() {
        List<Object[]> raw = orderService.getSalesHotStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Integer carId = (Integer) row[0];
            Long totalQty = (Long) row[1];
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("carId", carId);
            item.put("totalQuantity", totalQty);
            carRepository.findById(carId).ifPresent(car -> {
                item.put("brand", car.getBrand());
                item.put("model", car.getModel());
            });
            result.add(item);
        }
        return result;
    }

    /**
     * 销售额占比统计
     */
    public List<Map<String, Object>> getSalesShareStats() {
        List<Object[]> raw = orderService.getSalesShareStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : raw) {
            Integer carId = (Integer) row[0];
            java.math.BigDecimal totalRevenue = (java.math.BigDecimal) row[1];
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("carId", carId);
            item.put("totalRevenue", totalRevenue);
            carRepository.findById(carId).ifPresent(car -> {
                item.put("brand", car.getBrand());
                item.put("model", car.getModel());
            });
            result.add(item);
        }
        return result;
    }

    /**
     * 综合查询
     */
    public List<Map<String, Object>> querySales(String carType,
                                                 BigDecimal minPrice,
                                                 BigDecimal maxPrice,
                                                 String customerName,
                                                 LocalDateTime startDate,
                                                 LocalDateTime endDate) {
        List<PurchaseOrder> orders = orderService.findByFilters(carType, minPrice, maxPrice, customerName, startDate, endDate);
        return orders.stream().map(order -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("orderId", order.getOrderId());
            item.put("customerId", order.getCustomerId());
            if (order.getCustomer() != null) {
                item.put("customerName", order.getCustomer().getRealName());
                item.put("customerPhone", order.getCustomer().getPhone());
            }
            item.put("carId", order.getCarId());
            if (order.getCar() != null) {
                item.put("brand", order.getCar().getBrand());
                item.put("model", order.getCar().getModel());
                item.put("price", order.getCar().getPrice());
            }
            item.put("quantity", order.getQuantity());
            item.put("totalPrice", order.getTotalPrice());
            item.put("status", order.getStatus());
            item.put("createdAt", order.getCreatedAt());
            item.put("confirmedAt", order.getConfirmedAt());
            return item;
        }).collect(Collectors.toList());
    }
}
