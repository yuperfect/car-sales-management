package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.Customer;
import com.carsales.repository.CustomerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;

    public CustomerController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public ApiResponse<List<Customer>> findAll(
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            // 按姓名或电话模糊搜索
            List<Customer> customers = customerRepository.findAll()
                    .stream()
                    .filter(c -> c.getRealName().contains(keyword) || c.getPhone().contains(keyword))
                    .toList();
            return ApiResponse.success(customers);
        }
        return ApiResponse.success(customerRepository.findAll());
    }
}
