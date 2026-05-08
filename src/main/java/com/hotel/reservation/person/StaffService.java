package com.hotel.reservation.person;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final AdminRepository adminRepository;

    public StaffService(StaffRepository staffRepository, AdminRepository adminRepository) {
        this.staffRepository = staffRepository;
        this.adminRepository = adminRepository;
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public Staff getStaffById(Long id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found: " + id));
    }

    public Staff registerStaff(Staff staff) {
        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        return staffRepository.save(staff);
    }

    public Staff saveStaff(Staff staff) {
        return staffRepository.save(staff);
    }

    public void deleteStaff(Long id) {
        staffRepository.deleteById(id);
    }

    // Authentication — works for Staff AND Admin (since Admin extends Staff)
    public Optional<Staff> authenticate(String email, String password) {
        Optional<Staff> staff = staffRepository.findByEmail(email);
        if (staff.isPresent() && staff.get().login(email, password)) {
            return staff;
        }
        return Optional.empty();
    }

    public Optional<Admin> findAdminByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
}