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
    public ApiResponse<List<PurchaseOrder>> findAll(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ApiResponse.success(orderService.findByCustomerId(userId));
        }
        return ApiResponse.success(orderService.findAll());
    }

    @PostMapping
    public ApiResponse<PurchaseOrder> create(@RequestBody PurchaseOrder order) {
        try {
            return ApiResponse.success(orderService.create(order));
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
    public ApiResponse<PurchaseOrder> confirm(@PathVariable Integer id) {
        try {
            return ApiResponse.success(orderService.confirm(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
