package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.TestDrive;
import com.carsales.service.TestDriveService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test-drives")
public class TestDriveController {

    private final TestDriveService testDriveService;

    public TestDriveController(TestDriveService testDriveService) {
        this.testDriveService = testDriveService;
    }

    @GetMapping
    public ApiResponse<List<TestDrive>> findAll(@RequestParam(required = false) Integer userId) {
        if (userId != null) {
            return ApiResponse.success(testDriveService.findByCustomerId(userId));
        }
        return ApiResponse.success(testDriveService.findAll());
    }

    @PostMapping
    public ApiResponse<TestDrive> create(@RequestBody TestDrive testDrive) {
        try {
            return ApiResponse.success(testDriveService.create(testDrive));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/confirm")
    public ApiResponse<TestDrive> confirm(@PathVariable Integer id) {
        try {
            return ApiResponse.success(testDriveService.confirm(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<TestDrive> cancel(@PathVariable Integer id) {
        try {
            return ApiResponse.success(testDriveService.cancel(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
