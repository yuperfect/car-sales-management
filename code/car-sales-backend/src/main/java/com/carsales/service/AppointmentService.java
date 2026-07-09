package com.carsales.service;

import com.carsales.entity.Appointment;
import com.carsales.entity.Car;
import com.carsales.entity.Customer;
import com.carsales.repository.AppointmentRepository;
import com.carsales.repository.CarRepository;
import com.carsales.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final CarRepository carRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              CustomerRepository customerRepository,
                              CarRepository carRepository) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.carRepository = carRepository;
    }

    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> findById(Integer id) {
        return appointmentRepository.findById(id);
    }

    public List<Appointment> findByCustomerId(Integer customerId) {
        return appointmentRepository.findByCustomerId(customerId);
    }

    public List<Appointment> findByStatus(String status) {
        return appointmentRepository.findByStatus(status);
    }

    @Transactional
    public Appointment create(String customerName, String customerPhone, Integer carId, LocalDateTime appointmentTime, String remark) {
        // find or create customer
        Customer customer = customerRepository.findByPhone(customerPhone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setRealName(customerName);
                    newCustomer.setPhone(customerPhone);
                    newCustomer.setFirstSubmitTime(LocalDateTime.now());
                    return customerRepository.save(newCustomer);
                });

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found with id: " + carId));

        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setCar(car);
        appointment.setAppointmentTime(appointmentTime);
        appointment.setRemark(remark);
        appointment.setStatus("pending");
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment confirm(Integer id) {
        return appointmentRepository.findById(id).map(apt -> {
            if (!"pending".equals(apt.getStatus())) {
                throw new RuntimeException("Only pending appointment can be confirmed");
            }
            apt.setStatus("confirmed");
            apt.setHandleTime(LocalDateTime.now());
            return appointmentRepository.save(apt);
        }).orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }

    @Transactional
    public Appointment cancel(Integer id) {
        return appointmentRepository.findById(id).map(apt -> {
            if ("cancelled".equals(apt.getStatus())) {
                throw new RuntimeException("Appointment is already cancelled");
            }
            apt.setStatus("cancelled");
            apt.setHandleTime(LocalDateTime.now());
            return appointmentRepository.save(apt);
        }).orElseThrow(() -> new RuntimeException("Appointment not found with id: " + id));
    }

    public List<Object[]> getAppointmentHotStats() {
        return appointmentRepository.countByCarIdGrouped();
    }
}
