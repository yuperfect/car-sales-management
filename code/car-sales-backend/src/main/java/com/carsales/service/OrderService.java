package com.carsales.service;

import com.carsales.entity.Car;
import com.carsales.entity.Customer;
import com.carsales.entity.PurchaseOrder;
import com.carsales.enums.OrderStatus;
import com.carsales.repository.CarRepository;
import com.carsales.repository.CustomerRepository;
import com.carsales.repository.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    public OrderService(PurchaseOrderRepository purchaseOrderRepository,
                        CarRepository carRepository,
                        CustomerRepository customerRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
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
    public PurchaseOrder create(String customerName, String customerPhone, Integer carId, Integer quantity) {
        // validate phone
        if (customerPhone == null || !customerPhone.matches("1[3-9]\\d{9}")) {
            throw new RuntimeException("请输入正确的11位手机号");
        }
        // find or create customer
        Customer customer = customerRepository.findByPhone(customerPhone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setRealName(customerName);
                    newCustomer.setPhone(customerPhone);
                    newCustomer.setFirstSubmitTime(LocalDateTime.now());
                    return customerRepository.save(newCustomer);
                });

        // validate car exists and has enough stock
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + carId));

        if (car.getStock() < quantity) {
            throw new RuntimeException("Insufficient stock: available " + car.getStock() + ", requested " + quantity);
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setCustomer(customer);
        order.setCar(car);
        order.setQuantity(quantity);
        order.setUnitPrice(car.getPrice());
        order.setTotalAmount(car.getPrice().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus(OrderStatus.pending);
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder confirm(Integer id, String handler) {
        return purchaseOrderRepository.findById(id).map(order -> {
            if (order.getStatus() != OrderStatus.pending) {
                throw new RuntimeException("Only pending order can be confirmed");
            }

            // check stock again before confirming
            Car car = carRepository.findById(order.getCar().getCarId())
                    .orElseThrow(() -> new RuntimeException("Car not found with id: " + order.getCar().getCarId()));
            if (car.getStock() < order.getQuantity()) {
                throw new RuntimeException("Insufficient stock to confirm order");
            }

            // deduct stock
            car.setStock(car.getStock() - order.getQuantity());
            carRepository.save(car);

            order.setStatus(OrderStatus.confirmed);
            order.setHandleTime(LocalDateTime.now());
            order.setHandler(handler);
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
                Car car = carRepository.findById(order.getCar().getCarId())
                        .orElseThrow(() -> new RuntimeException("Car not found with id: " + order.getCar().getCarId()));
                car.setStock(car.getStock() + order.getQuantity());
                carRepository.save(car);
            }

            order.setStatus(OrderStatus.cancelled);
            order.setHandleTime(LocalDateTime.now());
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
                                              BigDecimal minPrice,
                                              BigDecimal maxPrice,
                                              String customerName,
                                              LocalDateTime startDate,
                                              LocalDateTime endDate) {
        return purchaseOrderRepository.findByFilters(carType, minPrice, maxPrice, customerName, startDate, endDate);
    }
}
