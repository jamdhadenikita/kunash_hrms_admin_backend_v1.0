package com.hrms.controller;

import com.hrms.config.BcryptEncoderConfig;
import com.hrms.dto.request.AdminRequestDto;
import com.hrms.dto.response.AdminResponseDto;
import com.hrms.entity.AdminEntity;
import com.hrms.repository.AdminRepository;
import com.hrms.service.AdminService;
import com.hrms.utils.AdminIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    private final AdminService adminService;
    private final AdminRepository adminRepository;
    private final BcryptEncoderConfig passwordEncoder;
    private final AdminIdGenerator adminIdGenerator;

    public AdminController(AdminService adminService, AdminRepository adminRepository, BcryptEncoderConfig passwordEncoder, AdminIdGenerator adminIdGenerator) {
        this.adminService = adminService;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminIdGenerator = adminIdGenerator;
    }

    // ────────────────────────────────────────────────
    //                  CREATE ADMIN
    // ────────────────────────────────────────────────
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping("/create-admin")
    public ResponseEntity<String> createAdmin(@RequestBody AdminRequestDto requestDto) {
        logger.info("POST /api/admin → Creating new admin: {}", requestDto.getAdminFirstName());

        try {
            AdminResponseDto created = adminService.createAdmin(requestDto);
            String message = "Admin created successfully with ID: " + created.getAdminId();
            logger.info(message);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (IllegalArgumentException e) {
            logger.warn("Validation failed during create: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating admin", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create admin. Please try again later.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  POST /api/admin/bootstrap
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/bootstrap")
    public ResponseEntity<?> bootstrap(@RequestBody Map<String, String> request) {

        logger.warn("Bootstrap endpoint called — checking if SUPER_ADMIN already exists");

        boolean superAdminExists = adminRepository.existsByAdminRole("SUPER_ADMIN");

        if (superAdminExists) {
            logger.warn("Bootstrap blocked — SUPER_ADMIN already exists in DB");
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Bootstrap already completed. This endpoint is now disabled."
            ));
        }

        String mobile   = request.get("mobile");
        String password = request.get("password");
        String firstName = request.getOrDefault("firstName", "Super");
        String lastName  = request.getOrDefault("lastName", "Admin");
        String uniqueAdminId = adminIdGenerator.generateUniqueAdminId();

        if (mobile == null || mobile.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "mobile is required"));
        }
        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "password must be at least 6 characters"));
        }

        if (adminRepository.findByAdminMobileNumber(mobile).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Mobile number already registered"));
        }

        AdminEntity superAdmin = new AdminEntity();
        superAdmin.setAdminId(uniqueAdminId);
        superAdmin.setAdminFirstName(firstName);
        superAdmin.setAdminLastName(lastName);
        superAdmin.setAdminMobileNumber(mobile);
        superAdmin.setAdminPassword(passwordEncoder.encode(password));
        superAdmin.setAdminRole("SUPER_ADMIN");
        superAdmin.setAdminIsActive(true);
        superAdmin.setAdminIsLocked(false);

        AdminEntity saved = adminRepository.save(superAdmin);

        logger.info("Bootstrap complete — SUPER_ADMIN created with ID: {}", saved.getAdminId());

        return ResponseEntity.status(201).body(Map.of(
                "message", "SUPER_ADMIN created successfully. This endpoint is now permanently disabled.",
                "adminId", saved.getAdminId(),
                "mobile",  mobile,
                "role",    "SUPER_ADMIN"
        ));
    }

    // ────────────────────────────────────────────────
    //                  GET ONE ADMIN
    // ────────────────────────────────────────────────
    @GetMapping("/get-admin-by-adminId/{adminId}")
    public ResponseEntity<?> getAdmin(@PathVariable String adminId) {
        logger.info("GET /api/admin/{} → Fetching admin", adminId);
        try {
            AdminResponseDto admin = adminService.getAdminByAdminId(adminId);
            return ResponseEntity.ok(admin);
        } catch (RuntimeException e) {
            logger.warn("Admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error fetching admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving admin details");
        }
    }

    // ────────────────────────────────────────────────
    //                  GET ALL ADMINS
    // ────────────────────────────────────────────────
    @GetMapping("/get-all-admin")
    public ResponseEntity<?> getAllAdmins() {
        logger.info("GET /api/admin → Fetching all admins");
        try {
            List<AdminResponseDto> admins = adminService.getAllAdmins();
            if (admins.isEmpty()) {
                return ResponseEntity.ok("No admins found.");
            }
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            logger.error("Error fetching all admins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve admins list");
        }
    }

    // ────────────────────────────────────────────────
    //                  FULL UPDATE (PUT)
    // ────────────────────────────────────────────────
    @PutMapping("/update-admin-by-adminId/{adminId}")
    public ResponseEntity<String> updateAdmin(
            @PathVariable String adminId,
            @RequestBody AdminRequestDto requestDto) {

        logger.info("PUT /api/admin/{} → Full update requested", adminId);
        try {
            AdminResponseDto updated = adminService.updateAdmin(adminId, requestDto);
            String message = "Admin updated successfully: " + adminId;
            logger.info(message);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.warn("Update failed - admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error updating admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update admin");
        }
    }

    // ────────────────────────────────────────────────
    //                  PARTIAL UPDATE (PATCH)
    // ────────────────────────────────────────────────
    @PatchMapping("/patch-admin-by-adminId/{adminId}")
    public ResponseEntity<String> patchAdmin(
            @PathVariable String adminId,
            @RequestBody AdminRequestDto requestDto) {

        logger.info("PATCH /api/admin/{} → Partial update requested", adminId);
        try {
            AdminResponseDto updated = adminService.patchAdmin(adminId, requestDto);
            String message = "Admin partially updated: " + adminId;
            logger.info(message);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.warn("Patch failed - admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error patching admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to apply partial update");
        }
    }

    // ────────────────────────────────────────────────
    //         NEW: UPDATE MODULE ACCESS (toggle grid)
    // ────────────────────────────────────────────────
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/update-admin-modules/{adminId}")
    public ResponseEntity<?> updateAdminModules(
            @PathVariable String adminId,
            @RequestBody Map<String, List<String>> request) {

        logger.info("PATCH /api/admin/update-admin-modules/{} → Module access update requested", adminId);
        try {
            List<String> moduleAccess = request.get("moduleAccess");
            AdminResponseDto updated = adminService.updateModuleAccess(adminId, moduleAccess);
            return ResponseEntity.ok(Map.of(
                    "message", "Module access updated successfully",
                    "adminId", updated.getAdminId(),
                    "moduleAccess", updated.getModuleAccess()
            ));
        } catch (IllegalArgumentException e) {
            logger.warn("Module access update rejected: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            logger.warn("Module access update failed - admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Admin not found with ID: " + adminId));
        } catch (Exception e) {
            logger.error("Error updating module access for {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update module access"));
        }
    }

    // ────────────────────────────────────────────────
    //                  DELETE ADMIN
    // ────────────────────────────────────────────────
    @DeleteMapping("/delete-admin-by-adminId/{adminId}")
    public ResponseEntity<String> deleteAdmin(@PathVariable String adminId) {
        logger.info("DELETE /api/admin/{} → Deleting admin", adminId);
        try {
            adminService.deleteAdmin(adminId);
            String message = "Admin deleted successfully: " + adminId;
            logger.info(message);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            logger.warn("Delete failed - admin not found: {}", adminId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Admin not found with ID: " + adminId);
        } catch (Exception e) {
            logger.error("Error deleting admin {}", adminId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete admin");
        }
    }
}