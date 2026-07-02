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
            car.setCategoryId(updated.getCategoryId());
            car.setBrand(updated.getBrand());
            car.setModel(updated.getModel());
            car.setYear(updated.getYear());
            car.setColor(updated.getColor());
            car.setPrice(updated.getPrice());
            car.setStock(updated.getStock());
            car.setDescription(updated.getDescription());
            car.setImageUrl(updated.getImageUrl());
            return carRepository.save(car);
        }).orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
    }

    public void deleteById(Integer id) {
        carRepository.deleteById(id);
    }

    public List<Car> findByStatus(CarStatus status) {
        return carRepository.findByStatus(status);
    }

    public List<Car> findByCategoryId(Integer categoryId) {
        return carRepository.findByCategoryId(categoryId);
    }

    public List<Car> findByFilters(Integer categoryId, String brand, BigDecimal minPrice, BigDecimal maxPrice) {
        return carRepository.findByFilters(categoryId, brand, minPrice, maxPrice);
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
