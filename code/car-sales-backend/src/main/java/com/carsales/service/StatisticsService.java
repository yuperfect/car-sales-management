package com.carsales.service;

import com.carsales.entity.Car;
import com.carsales.entity.PurchaseOrder;
import com.carsales.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    private final AppointmentService appointmentService;
    private final OrderService orderService;
    private final CarRepository carRepository;

    public StatisticsService(AppointmentService appointmentService,
                             OrderService orderService,
                             CarRepository carRepository) {
        this.appointmentService = appointmentService;
        this.orderService = orderService;
        this.carRepository = carRepository;
    }

    /**
     * 试驾预约热点统计：按车型分组统计预约次数
     */
    public List<Map<String, Object>> getAppointmentHotStats() {
        List<Object[]> raw = appointmentService.getAppointmentHotStats();
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
            item.put("count", totalQty);         // 前端期望 count
            item.put("totalQuantity", totalQty);  // 向下兼容
            carRepository.findById(carId).ifPresent(car -> {
                item.put("name", car.getBrand() + " " + car.getModel());  // 前端期望 name
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
            BigDecimal totalRevenue = (BigDecimal) row[1];
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("carId", carId);
            item.put("amount", totalRevenue);     // 前端期望 amount
            item.put("totalRevenue", totalRevenue); // 向下兼容
            carRepository.findById(carId).ifPresent(car -> {
                item.put("name", car.getBrand() + " " + car.getModel());  // 前端期望 name
                item.put("brand", car.getBrand());
                item.put("model", car.getModel());
            });
            result.add(item);
        }
        return result;
    }

    /**
     * 热销价格区间统计
     * 返回格式: [{range: "10万以下", count: N}, {range: "10万-20万", count: N}, ...]
     */
    public List<Map<String, Object>> getPriceRangeStats() {
        List<PurchaseOrder> confirmedOrders = orderService.findByStatus(com.carsales.enums.OrderStatus.confirmed);

        int range1 = 0, range2 = 0, range3 = 0, range4 = 0, range5 = 0;

        for (PurchaseOrder order : confirmedOrders) {
            BigDecimal unitPrice = order.getUnitPrice();
            if (unitPrice == null) continue;
            if (unitPrice.compareTo(new BigDecimal("100000")) < 0) {
                range1 += order.getQuantity();
            } else if (unitPrice.compareTo(new BigDecimal("200000")) < 0) {
                range2 += order.getQuantity();
            } else if (unitPrice.compareTo(new BigDecimal("300000")) < 0) {
                range3 += order.getQuantity();
            } else if (unitPrice.compareTo(new BigDecimal("500000")) < 0) {
                range4 += order.getQuantity();
            } else {
                range5 += order.getQuantity();
            }
        }

        // 改为数组格式，前端期望 [{range, count}, ...]
        List<Map<String, Object>> result = new ArrayList<>();
        addRangeItem(result, "10万以下", range1);
        addRangeItem(result, "10万-20万", range2);
        addRangeItem(result, "20万-30万", range3);
        addRangeItem(result, "30万-50万", range4);
        addRangeItem(result, "50万以上", range5);
        return result;
    }

    private void addRangeItem(List<Map<String, Object>> list, String range, int count) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("range", range);
        item.put("count", count);
        list.add(item);
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
                item.put("unitPrice", order.getCar().getPrice());
            }
            item.put("quantity", order.getQuantity());
            item.put("unitPrice", order.getUnitPrice());
            item.put("totalAmount", order.getTotalAmount());
            item.put("status", order.getStatus());
            item.put("orderTime", order.getOrderTime());
            item.put("handleTime", order.getHandleTime());
            item.put("handler", order.getHandler());
            return item;
        }).collect(Collectors.toList());
    }
}
