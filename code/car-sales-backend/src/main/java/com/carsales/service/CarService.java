package com.carsales.service;

import com.carsales.entity.Car;
import com.carsales.enums.CarStatus;
import com.carsales.repository.AppointmentRepository;
import com.carsales.repository.CarRepository;
import com.carsales.repository.PurchaseOrderRepository;
import com.carsales.util.ExcelImportUtil.CarImportRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final AppointmentRepository appointmentRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public CarService(CarRepository carRepository,
                      AppointmentRepository appointmentRepository,
                      PurchaseOrderRepository purchaseOrderRepository) {
        this.carRepository = carRepository;
        this.appointmentRepository = appointmentRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
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

    /**
     * 级联删除车辆：先删关联的预约和订单，再删图片文件和车辆本身
     */
    @Transactional
    public void deleteCascading(Integer id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("车辆不存在，ID: " + id));
        // 删关联预约
        appointmentRepository.deleteByCarId(id);
        // 删关联订单
        purchaseOrderRepository.deleteByCarId(id);
        // 删图片文件
        deleteImageFile(car.getImageUrl());
        // 删车辆
        carRepository.delete(car);
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

    /**
     * 批量保存车辆（含可选的嵌入图片）
     * 逐条保存（需要自增 ID 来命名图片文件）
     *
     * @return 成功保存的数量
     */
    @Transactional
    public int saveAllWithImages(List<CarImportRow> rows) {
        int saved = 0;
        for (CarImportRow row : rows) {
            try {
                Car car = row.getCar();
                if (car.getBrand() == null || car.getBrand().isBlank()) continue;

                if (row.hasImage()) {
                    // 先保存车，拿到 ID
                    Car savedCar = carRepository.save(car);
                    // 存图片
                    String ext = row.getImageExtension();
                    if (!ALLOWED_EXTENSIONS.contains(ext)) {
                        ext = "jpg";
                    }
                    String filename = savedCar.getCarId() + "." + ext;
                    String imageUrl = saveImageBytes(savedCar.getCarId(), row.getImageData(), ext);
                    savedCar.setImageUrl(imageUrl);
                    carRepository.save(savedCar);
                } else {
                    carRepository.save(car);
                }
                saved++;
            } catch (Exception e) {
                System.err.println("导入车辆图片失败（跳过）: " + e.getMessage());
                // 跳过失败的图片，继续下一辆
            }
        }
        return saved;
    }

    // ========== Image-aware overloaded methods ==========

    /**
     * 保存车辆（含可选图片）
     * 如果 imageFile 非空，保存图片到 uploads/images/{carId}.{ext} 并设置 imageUrl
     */
    public Car save(Car car, MultipartFile imageFile) {
        // 先保存车辆以获取自增 ID
        Car savedCar = carRepository.save(car);

        if (imageFile != null && !imageFile.isEmpty()) {
            validateImageFile(imageFile);
            String imageUrl = saveImageFile(savedCar.getCarId(), imageFile);
            savedCar.setImageUrl(imageUrl);
            savedCar = carRepository.save(savedCar);
        }

        return savedCar;
    }

    /**
     * 更新车辆（含可选图片处理）
     * - imageFile == null：保持原有图片不变
     * - imageFile 非空但不为空：替换为新图
     * - imageFile 非但 isEmpty()==true：移图（image_url 置 NULL，删除文件）
     */
    public Car update(Integer id, Car updated, MultipartFile imageFile) {
        return carRepository.findById(id).map(car -> {
            // 更新文本字段
            if (updated.getBrand() != null) car.setBrand(updated.getBrand());
            if (updated.getModel() != null) car.setModel(updated.getModel());
            if (updated.getDisplacement() != null) car.setDisplacement(updated.getDisplacement());
            if (updated.getTransmission() != null) car.setTransmission(updated.getTransmission());
            if (updated.getColor() != null) car.setColor(updated.getColor());
            if (updated.getPrice() != null) car.setPrice(updated.getPrice());
            if (updated.getStock() != null) car.setStock(updated.getStock());
            if (updated.getStatus() != null) car.setStatus(updated.getStatus());

            // 图片处理
            if (imageFile != null) {
                if (imageFile.isEmpty()) {
                    // 显式传空文件 → 移除图片
                    deleteImageFile(car.getImageUrl());
                    car.setImageUrl(null);
                } else {
                    // 上传新图片 → 替换旧图
                    validateImageFile(imageFile);
                    deleteImageFile(car.getImageUrl());
                    String imageUrl = saveImageFile(car.getCarId(), imageFile);
                    car.setImageUrl(imageUrl);
                }
            }
            // imageFile == null → 不处理图片，保持原样

            return carRepository.save(car);
        }).orElseThrow(() -> new RuntimeException("Car not found with id: " + id));
    }

    // ========== Private image helpers ==========

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/car-sales-uploads/images";

    /**
     * 启动时将旧目录（工作目录相对路径）下的图片迁移到新目录（用户主目录）
     */
    @PostConstruct
    public void migrateOldImages() {
        try {
            Path oldDir = Paths.get("uploads/images");
            Path newDir = Paths.get(UPLOAD_DIR);
            if (Files.exists(oldDir)) {
                Files.createDirectories(newDir);
                try (var files = Files.list(oldDir)) {
                    files.filter(f -> !f.getFileName().toString().equals(".gitkeep"))
                         .forEach(f -> {
                             try {
                                 Path target = newDir.resolve(f.getFileName());
                                 if (!Files.exists(target)) {
                                     Files.copy(f, target, StandardCopyOption.REPLACE_EXISTING);
                                 }
                             } catch (IOException e) {
                                 System.err.println("迁移图片失败: " + f + " - " + e.getMessage());
                             }
                         });
                }
                System.out.println("图片迁移完成: " + oldDir + " → " + newDir);
            }
        } catch (IOException e) {
            System.err.println("检查旧图片目录时出错: " + e.getMessage());
        }
    }

    /**
     * 校验图片文件格式
     */
    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            return;
        }
        String ext = getExtension(imageFile);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/WebP 格式的图片，当前格式: " + ext);
        }
    }

    /**
     * 保存图片文件到 uploads/images/{carId}.{ext}
     * 返回可公开访问的相对路径 /uploads/images/{filename}
     */
    private String saveImageFile(Integer carId, MultipartFile imageFile) {
        String ext = getExtension(imageFile);
        String filename = carId + "." + ext;

        try {
            Path uploadDir = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(filename);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败: " + e.getMessage(), e);
        }
    }

    /**
     * 保存图片二进制数据到 uploads/images/{carId}.{ext}
     * 用于 Excel 导入时嵌入的图片
     */
    private String saveImageBytes(Integer carId, byte[] imageData, String ext) {
        String filename = carId + "." + ext;
        try {
            Path uploadDir = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(filename);
            Files.write(filePath, imageData);
            return "/images/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("图片保存失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除图片文件
     */
    private void deleteImageFile(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        try {
            // imageUrl 格式: /images/{filename} → 实际路径: uploads/images/{filename}
            String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // 文件删除为最佳尝试，不阻塞主流程
            System.err.println("图片文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 从 MultipartFile 中提取扩展名（小写）
     * 无法获取扩展名时默认返回 "jpg"
     */
    private String getExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return "jpg";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "jpg";
        }
        return originalFilename.substring(dotIndex + 1).toLowerCase();
    }
}
