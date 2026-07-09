package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.PurchaseOrder;
import com.carsales.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ApiResponse<List<PurchaseOrder>> findAll(@RequestParam(required = false) Integer customerId) {
        if (customerId != null) {
            return ApiResponse.success(orderService.findByCustomerId(customerId));
        }
        return ApiResponse.success(orderService.findAll());
    }

    @PostMapping
    public ApiResponse<PurchaseOrder> create(@RequestParam String customerName,
                                              @RequestParam String customerPhone,
                                              @RequestParam Integer carId,
                                              @RequestParam(defaultValue = "1") Integer quantity) {
        try {
            return ApiResponse.success(orderService.create(customerName, customerPhone, carId, quantity));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        try {
            orderService.deleteById(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<PurchaseOrder> confirm(@PathVariable Integer id,
                                               @RequestParam(required = false, defaultValue = "admin") String handler) {
        try {
            return ApiResponse.success(orderService.confirm(id, handler));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
