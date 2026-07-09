package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.Car;
import com.carsales.service.CarService;
import com.carsales.util.ExcelImportUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return ApiResponse.success(carService.findByKeyword(keyword));
        }
        if ((brand != null && !brand.isBlank()) || (model != null && !model.isBlank()) || minPrice != null || maxPrice != null) {
            return ApiResponse.success(carService.findByFilters(brand, model, minPrice, maxPrice));
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

    @GetMapping("/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"车辆导入模板.xlsx\"");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("车辆数据");
            String[] headers = {"品牌", "车型", "排量", "变速箱", "颜色", "价格", "库存"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
                sheet.autoSizeColumn(i);
            }
            workbook.write(response.getOutputStream());
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
