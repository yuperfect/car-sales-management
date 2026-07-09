package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.Appointment;
import com.carsales.service.AppointmentService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ApiResponse<List<Appointment>> findAll(@RequestParam(required = false) Integer customerId) {
        if (customerId != null) {
            return ApiResponse.success(appointmentService.findByCustomerId(customerId));
        }
        return ApiResponse.success(appointmentService.findAll());
    }

    @PostMapping
    public ApiResponse<Appointment> create(@RequestParam String customerName,
                                            @RequestParam String customerPhone,
                                            @RequestParam Integer carId,
                                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentTime,
                                            @RequestParam(required = false) String remark) {
        try {
            return ApiResponse.success(appointmentService.create(customerName, customerPhone, carId, appointmentTime, remark));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<Appointment> confirm(@PathVariable Integer id) {
        try {
            return ApiResponse.success(appointmentService.confirm(id));
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
}
