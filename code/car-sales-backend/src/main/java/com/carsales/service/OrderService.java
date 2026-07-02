package com.carsales.service;

import com.carsales.entity.Car;
import com.carsales.entity.PurchaseOrder;
import com.carsales.enums.OrderStatus;
import com.carsales.repository.CarRepository;
import com.carsales.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CarRepository carRepository;

    public OrderService(PurchaseOrderRepository purchaseOrderRepository,
                        CarRepository carRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.carRepository = carRepository;
    }

    public List<PurchaseOrder> findAll() {
        return purchaseOrderRepository.findAll();
    }

    public Optional<PurchaseOrder> findById(Integer id) {
        return purchaseOrderRepository.findById(id);
    }

    public List<PurchaseOrder> findByCustomerId(Integer customerId) {
        return purchaseOrderRepository.findByCustomerId(customerId);
    }

    public List<PurchaseOrder> findByStatus(OrderStatus status) {
        return purchaseOrderRepository.findByStatus(status);
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrder order) {
        // validate car exists and has enough stock
        Car car = carRepository.findById(order.getCarId())
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + order.getCarId()));

        if (car.getStock() < order.getQuantity()) {
            throw new RuntimeException("Insufficient stock: available " + car.getStock() + ", requested " + order.getQuantity());
        }

        // calculate total price
        order.setTotalPrice(car.getPrice().multiply(java.math.BigDecimal.valueOf(order.getQuantity())));
        order.setStatus(OrderStatus.pending);
        order.setCreatedAt(LocalDateTime.now());
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder confirm(Integer id) {
        return purchaseOrderRepository.findById(id).map(order -> {
            if (order.getStatus() != OrderStatus.pending) {
                throw new RuntimeException("Only pending order can be confirmed");
            }

            // check stock again before confirming
            Car car = carRepository.findById(order.getCarId())
                    .orElseThrow(() -> new RuntimeException("Car not found with id: " + order.getCarId()));
            if (car.getStock() < order.getQuantity()) {
                throw new RuntimeException("Insufficient stock to confirm order");
            }

            // deduct stock
            car.setStock(car.getStock() - order.getQuantity());
            carRepository.save(car);

            order.setStatus(OrderStatus.confirmed);
            order.setConfirmedAt(LocalDateTime.now());
            return purchaseOrderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Transactional
    public PurchaseOrder cancel(Integer id) {
        return purchaseOrderRepository.findById(id).map(order -> {
            if (order.getStatus() == OrderStatus.cancelled) {
                throw new RuntimeException("Order is already cancelled");
            }

            // restore stock if was confirmed
            if (order.getStatus() == OrderStatus.confirmed) {
                Car car = carRepository.findById(order.getCarId())
                        .orElseThrow(() -> new RuntimeException("Car not found with id: " + order.getCarId()));
                car.setStock(car.getStock() + order.getQuantity());
                carRepository.save(car);
            }

            order.setStatus(OrderStatus.cancelled);
            return purchaseOrderRepository.save(order);
        }).orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Transactional
    public void deleteById(Integer id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        if (order.getStatus() != OrderStatus.pending) {
            throw new RuntimeException("Only pending order can be deleted");
        }
        purchaseOrderRepository.deleteById(id);
    }

    public List<Object[]> getSalesHotStats() {
        return purchaseOrderRepository.countSalesByCarIdGrouped();
    }

    public List<Object[]> getSalesShareStats() {
        return purchaseOrderRepository.sumRevenueByCarIdGrouped();
    }

    public List<PurchaseOrder> findByFilters(String carType,
                                              java.math.BigDecimal minPrice,
                                              java.math.BigDecimal maxPrice,
                                              String customerName,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate) {
        return purchaseOrderRepository.findByFilters(carType, minPrice, maxPrice, customerName, startDate, endDate);
    }
}
