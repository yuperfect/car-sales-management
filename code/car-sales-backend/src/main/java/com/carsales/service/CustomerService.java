package com.carsales.service;

import com.carsales.entity.Customer;
import com.carsales.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    private static final String AVATAR_DIR = System.getProperty("user.home") + "/car-sales-uploads/avatars";

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // ========== SHA-256 密码加密 ==========

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    // ========== 绑定（新用户注册 / 现有客户补全身份） ==========

    @Transactional
    public Customer bind(String username, String password, String realName, String phone) {
        // 校验用户名唯一
        if (customerRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("用户名已被占用");
        }

        // 按手机号匹配现有客户
        Customer customer = customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setRealName(realName);
                    newCustomer.setPhone(phone);
                    newCustomer.setFirstSubmitTime(LocalDateTime.now());
                    return newCustomer;
                });

        // 如果已绑定则拒绝
        if (customer.getUsername() != null && !customer.getUsername().equals(username)) {
            throw new RuntimeException("该手机号已绑定其他账号");
        }

        customer.setUsername(username);
        customer.setPassword(hashPassword(password));
        customer.setRealName(realName);
        customer.setUpdateTime(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    // ========== 登录验证 ==========

    public Customer login(String username, String password) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));

        if (!customer.getPassword().equals(hashPassword(password))) {
            throw new RuntimeException("用户名或密码错误");
        }
        return customer;
    }

    // ========== 查询 ==========

    public Customer getCustomer(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("客户不存在，ID: " + id));
    }

    public List<Customer> findAll(String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return customerRepository.findByUsernameContainingOrRealNameContainingOrPhoneContaining(
                    keyword, keyword, keyword);
        }
        return customerRepository.findAll();
    }

    // ========== 更新个人信息 ==========

    @Transactional
    public Customer updateCustomer(Integer id, String realName, String phone,
                                    String email, String address, String gender,
                                    LocalDate birthday, MultipartFile avatarFile) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("客户不存在，ID: " + id));

        if (realName != null) customer.setRealName(realName);
        if (phone != null) {
            if (!phone.matches("\\d{11}")) {
                throw new RuntimeException("请输入正确的11位手机号");
            }
            customer.setPhone(phone);
        }
        if (email != null) customer.setEmail(email);
        if (address != null) customer.setAddress(address);
        if (gender != null) customer.setGender(gender);
        if (birthday != null) customer.setBirthday(birthday);

        // 头像处理
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String avatarUrl = saveAvatarFile(id, avatarFile);
            customer.setAvatarUrl(avatarUrl);
        } else if (avatarFile != null && avatarFile.isEmpty()) {
            // 空文件 = 删除头像
            deleteAvatarFile(customer.getAvatarUrl());
            customer.setAvatarUrl(null);
        }

        customer.setUpdateTime(LocalDateTime.now());
        return customerRepository.save(customer);
    }

    // ========== 头像文件处理 ==========

    private String saveAvatarFile(Integer customerId, MultipartFile file) {
        String ext = getExtension(file);
        String filename = customerId + "." + ext;
        try {
            Path uploadDir = Paths.get(AVATAR_DIR);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/avatars/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("头像保存失败: " + e.getMessage(), e);
        }
    }

    private void deleteAvatarFile(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) return;
        try {
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(AVATAR_DIR, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("头像文件删除失败: " + e.getMessage());
        }
    }

    private String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) return "jpg";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) return "jpg";
        return originalFilename.substring(dotIndex + 1).toLowerCase();
    }
}
