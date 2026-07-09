package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.PurchaseOrder;
import com.carsales.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public ApiResponse<PurchaseOrder> findById(@PathVariable Integer id) {
        return orderService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("Order not found with id: " + id));
    }

    @PostMapping
    public ApiResponse<PurchaseOrder> create(@RequestBody Map<String, Object> body) {
        try {
            String customerName = (String) body.get("customerName");
            String customerPhone = (String) body.get("customerPhone");
            Integer carId = Integer.valueOf(body.get("carId").toString());
            Integer quantity = body.containsKey("quantity") ? Integer.valueOf(body.get("quantity").toString()) : 1;
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
                                               @RequestBody Map<String, Object> body) {
        try {
            String handler = (String) body.getOrDefault("handler", "admin");
            return ApiResponse.success(orderService.confirm(id, handler));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<PurchaseOrder> cancel(@PathVariable Integer id) {
        try {
            return ApiResponse.success(orderService.cancel(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
