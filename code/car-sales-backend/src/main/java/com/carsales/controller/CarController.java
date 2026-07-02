package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.Car;
import com.carsales.service.CarService;
import com.carsales.util.ExcelImportUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public ApiResponse<List<Car>> findAll(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        if (categoryId != null || (brand != null && !brand.isBlank()) || minPrice != null || maxPrice != null) {
            return ApiResponse.success(carService.findByFilters(categoryId, brand, minPrice, maxPrice));
        }
        return ApiResponse.success(carService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Car> findById(@PathVariable Integer id) {
        return carService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("Car not found with id: " + id));
    }

    @PostMapping
    public ApiResponse<Car> create(@RequestBody Car car) {
        try {
            return ApiResponse.success(carService.save(car));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<Car> update(@PathVariable Integer id, @RequestBody Car car) {
        try {
            return ApiResponse.success(carService.update(id, car));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Integer id) {
        try {
            carService.deleteById(id);
            return ApiResponse.success(null);
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/import")
    public ApiResponse<String> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<Car> cars = ExcelImportUtil.parseCars(file);
            carService.saveAll(cars);
            return ApiResponse.success("Successfully imported " + cars.size() + " cars");
        } catch (Exception e) {
            return ApiResponse.error("Import failed: " + e.getMessage());
        }
    }
}
