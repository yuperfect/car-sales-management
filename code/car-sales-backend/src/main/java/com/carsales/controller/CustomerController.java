package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.Customer;
import com.carsales.service.CustomerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * 根据用户名查找或创建客户（无需密码）
     */
    @PostMapping("/switch")
    public ApiResponse<Customer> findOrCreate(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            if (username == null || username.isBlank()) {
                return ApiResponse.error("用户名不能为空");
            }
            return ApiResponse.success(customerService.findOrCreate(username));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 客户列表（管理端用）
     */
    @GetMapping
    public ApiResponse<List<Customer>> findAll(
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(customerService.findAll(keyword));
    }

    /**
     * 获取单个客户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Customer> findById(@PathVariable Integer id) {
        try {
            return ApiResponse.success(customerService.getCustomer(id));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 绑定（注册/补全身份）
     */
    @PostMapping("/bind")
    public ApiResponse<Customer> bind(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");
            String realName = body.get("realName");
            String phone = body.get("phone");

            if (username == null || username.isBlank()) throw new RuntimeException("用户名不能为空");
            if (password == null || password.isBlank()) throw new RuntimeException("密码不能为空");
            if (realName == null || realName.isBlank()) throw new RuntimeException("姓名不能为空");
            if (phone == null || !phone.matches("\\d{11}")) throw new RuntimeException("请输入正确的11位手机号");

            return ApiResponse.success(customerService.bind(username, password, realName, phone));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 登录验证
     */
    @PostMapping("/login")
    public ApiResponse<Customer> login(@RequestBody Map<String, String> body) {
        try {
            String username = body.get("username");
            String password = body.get("password");

            if (username == null || username.isBlank()) throw new RuntimeException("用户名不能为空");
            if (password == null || password.isBlank()) throw new RuntimeException("密码不能为空");

            return ApiResponse.success(customerService.login(username, password));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 更新个人信息（含头像）
     */
    @PutMapping("/{id}")
    public ApiResponse<Customer> update(@PathVariable Integer id,
                                         @RequestParam(required = false) String realName,
                                         @RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String email,
                                         @RequestParam(required = false) String address,
                                         @RequestParam(required = false) String gender,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
                                         @RequestParam(required = false) MultipartFile avatar) {
        try {
            return ApiResponse.success(customerService.updateCustomer(id, realName, phone, email, address, gender, birthday, avatar));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
