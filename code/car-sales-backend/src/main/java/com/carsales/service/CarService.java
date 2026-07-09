package com.carsales.service;

import com.carsales.entity.Car;
import com.carsales.enums.CarStatus;
import com.carsales.repository.CarRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public List<Car> findAll() {
        return carRepository.findAll();
    }

    public Optional<Car> findById(Integer id) {
        return carRepository.findById(id);
    }

    public Car save(Car car) {
        return carRepository.save(car);
    }

    public Car update(Integer id, Car updated) {
        return carRepository.findById(id).map(car -> {
            // 仅更新前端传入的非空字段（支持部分更新）
            if (updated.getBrand() != null) car.setBrand(updated.getBrand());
            if (updated.getModel() != null) car.setModel(updated.getModel());
            if (updated.getDisplacement() != null) car.setDisplacement(updated.getDisplacement());
            if (updated.getTransmission() != null) car.setTransmission(updated.getTransmission());
            if (updated.getColor() != null) car.setColor(updated.getColor());
            if (updated.getPrice() != null) car.setPrice(updated.getPrice());
            if (updated.getStock() != null) car.setStock(updated.getStock());
            if (updated.getStatus() != null) car.setStatus(updated.getStatus());
            if (updated.getListedTime() != null) car.setListedTime(updated.getListedTime());
            return carRepository.save(car);
        }).orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
    }

    public void deleteById(Integer id) {
        carRepository.deleteById(id);
    }

    public List<Car> findByStatus(CarStatus status) {
        return carRepository.findByStatus(status);
    }

    public List<Car> findByFilters(String brand, String model, BigDecimal minPrice, BigDecimal maxPrice) {
        return carRepository.findByFilters(brand, model, minPrice, maxPrice);
    }

    public List<Car> findByKeyword(String keyword) {
        return carRepository.findByKeyword(keyword);
    }

    @Transactional
    public boolean deductStock(Integer carId, int quantity) {
        return carRepository.findById(carId).map(car -> {
            if (car.getStock() < quantity) {
                return false;
            }
            car.setStock(car.getStock() - quantity);
            carRepository.save(car);
            return true;
        }).orElse(false);
    }

    @Transactional
    public void restoreStock(Integer carId, int quantity) {
        carRepository.findById(carId).ifPresent(car -> {
            car.setStock(car.getStock() + quantity);
            carRepository.save(car);
        });
    }

    @Transactional
    public void saveAll(List<Car> cars) {
        carRepository.saveAll(cars);
    }
}
