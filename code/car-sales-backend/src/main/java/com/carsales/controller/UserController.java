package com.carsales.controller;

import com.carsales.dto.ApiResponse;
import com.carsales.entity.User;
import com.carsales.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<User>> findAll() {
        return ApiResponse.success(userService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<User> findById(@PathVariable Integer id) {
        return userService.findById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("User not found with id: " + id));
    }

    @PutMapping("/{id}")
    public ApiResponse<User> update(@PathVariable Integer id, @RequestBody User user) {
        try {
            return ApiResponse.success(userService.update(id, user));
        } catch (RuntimeException e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
