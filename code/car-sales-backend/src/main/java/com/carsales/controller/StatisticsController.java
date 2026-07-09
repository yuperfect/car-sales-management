package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/api/statistics/appointment-hot")
    public ApiResponse<List<Map<String, Object>>> getAppointmentHotStats() {
        return ApiResponse.success(statisticsService.getAppointmentHotStats());
    }

    @GetMapping("/api/statistics/sales-hot")
    public ApiResponse<List<Map<String, Object>>> getSalesHotStats() {
        return ApiResponse.success(statisticsService.getSalesHotStats());
    }

    @GetMapping("/api/statistics/sales-share")
    public ApiResponse<List<Map<String, Object>>> getSalesShareStats() {
        return ApiResponse.success(statisticsService.getSalesShareStats());
    }

    @GetMapping("/api/statistics/price-range")
    public ApiResponse<List<Map<String, Object>>> getPriceRangeStats() {
        return ApiResponse.success(statisticsService.getPriceRangeStats());
    }

    @GetMapping("/api/queries/sales")
    public ApiResponse<List<Map<String, Object>>> querySales(
            @RequestParam(required = false) String carType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String mode) {
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        return ApiResponse.success(statisticsService.querySales(carType, minPrice, maxPrice, customerName, startDateTime, endDateTime, mode));
    }
}
