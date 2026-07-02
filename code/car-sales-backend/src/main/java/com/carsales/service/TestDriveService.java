package com.carsales.service;

import com.carsales.entity.TestDrive;
import com.carsales.enums.OrderStatus;
import com.carsales.repository.TestDriveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TestDriveService {

    private final TestDriveRepository testDriveRepository;

    public TestDriveService(TestDriveRepository testDriveRepository) {
        this.testDriveRepository = testDriveRepository;
    }

    public List<TestDrive> findAll() {
        return testDriveRepository.findAll();
    }

    public Optional<TestDrive> findById(Integer id) {
        return testDriveRepository.findById(id);
    }

    public List<TestDrive> findByCustomerId(Integer customerId) {
        return testDriveRepository.findByCustomerId(customerId);
    }

    public List<TestDrive> findByStatus(OrderStatus status) {
        return testDriveRepository.findByStatus(status);
    }

    @Transactional
    public TestDrive create(TestDrive testDrive) {
        testDrive.setStatus(OrderStatus.pending);
        testDrive.setCreatedAt(LocalDateTime.now());
        return testDriveRepository.save(testDrive);
    }

    @Transactional
    public TestDrive confirm(Integer id) {
        return testDriveRepository.findById(id).map(drive -> {
            if (drive.getStatus() != OrderStatus.pending) {
                throw new RuntimeException("Only pending test drive can be confirmed");
            }
            drive.setStatus(OrderStatus.confirmed);
            drive.setConfirmedAt(LocalDateTime.now());
            return testDriveRepository.save(drive);
        }).orElseThrow(() -> new RuntimeException("Test drive not found with id: " + id));
    }

    @Transactional
    public TestDrive cancel(Integer id) {
        return testDriveRepository.findById(id).map(drive -> {
            if (drive.getStatus() == OrderStatus.cancelled) {
                throw new RuntimeException("Test drive is already cancelled");
            }
            drive.setStatus(OrderStatus.cancelled);
            return testDriveRepository.save(drive);
        }).orElseThrow(() -> new RuntimeException("Test drive not found with id: " + id));
    }

    public List<Object[]> getTestDriveHotStats() {
        return testDriveRepository.countByCarIdGrouped();
    }
}
