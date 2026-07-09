package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.Appointment;
import com.carsales.service.AppointmentService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ApiResponse<List<Appointment>> findAll(
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) String status) {
        if (customerId != null) {
            return ApiResponse.success(appointmentService.findByCustomerId(customerId));
        }
        if (status != null && !status.isBlank()) {
            return ApiResponse.success(appointmentService.findByStatus(status));
        }
        return ApiResponse.success(appointmentService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Appointment> findById(@PathVariable Integer id) {
        return appointmentService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("Appointment not found with id: " + id));
    }

    @PostMapping
    public ApiResponse<Appointment> create(@RequestBody Map<String, Object> body) {
        try {
            String customerName = (String) body.get("customerName");
            String customerPhone = (String) body.get("customerPhone");
            Integer carId = Integer.valueOf(body.get("carId").toString());
            LocalDateTime appointmentTime = LocalDateTime.parse((String) body.get("appointmentTime"));
            String remark = (String) body.getOrDefault("remark", null);
            return ApiResponse.success(appointmentService.create(customerName, customerPhone, carId, appointmentTime, remark));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<Appointment> confirm(@PathVariable Integer id,
                                             @RequestBody(required = false) Map<String, Object> body) {
        try {
            String handler = body != null ? (String) body.getOrDefault("handler", "admin") : "admin";
            return ApiResponse.success(appointmentService.confirm(id, handler));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<Appointment> cancel(@PathVariable Integer id) {
        try {
            return ApiResponse.success(appointmentService.cancel(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/reject")
    public ApiResponse<Appointment> reject(@PathVariable Integer id) {
        try {
            return ApiResponse.success(appointmentService.cancel(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
