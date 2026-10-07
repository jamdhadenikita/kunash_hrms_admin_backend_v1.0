package com.hrms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.request.DashboardDTO;
import com.hrms.dto.request.EmployeeRequestDTO;
import com.hrms.dto.request.RegisterEmployeeRequestDTO;
import com.hrms.dto.response.*;
import com.hrms.entity.EmployeeEntity;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.EmployeeService;
import com.hrms.utils.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {


    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    private final EmployeeService employeeService;
    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeService employeeService, EmployeeRepository employeeRepository) {
        this.employeeService = employeeService;
        this.employeeRepository = employeeRepository;
    }


    // CREATE Employee
    @PostMapping(value = "/create-employee", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> createEmployee(
            @RequestPart("employee") String employeeJson,
            @RequestPart(value = "aadhaarDocument", required = false) MultipartFile aadhaarDocument,
            @RequestPart(value = "panDocument", required = false) MultipartFile panDocument,
            @RequestPart(value = "degreeDocument", required = false) MultipartFile degreeDocument,
            @RequestPart(value = "experienceDocument", required = false) MultipartFile experienceDocument,
            @RequestPart(value = "offerLetter", required = false) MultipartFile offerLetter,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        try {
            EmployeeRequestDTO dto = objectMapper.readValue(employeeJson, EmployeeRequestDTO.class);

            dto.setAadhaarDocument(aadhaarDocument);
            dto.setPanDocument(panDocument);
            dto.setDegreeDocument(degreeDocument);
            dto.setExperienceDocument(experienceDocument);
            dto.setOfferLetter(offerLetter);
            dto.setProfilePhoto(profilePhoto);

            EmployeeResponseDTO employee = employeeService.createEmployee(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee created successfully");
            response.put("data", employee);
            return new ResponseEntity<>(response, HttpStatus.CREATED);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    // UPDATE Employee
    @PutMapping(value = "/update-employee/{employeePrimeId}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, Object>> updateEmployee(
            @PathVariable Long employeePrimeId,
            @RequestPart("employee") String employeeJson,
            @RequestPart(value = "aadhaarDocument", required = false) MultipartFile aadhaarDocument,
            @RequestPart(value = "panDocument", required = false) MultipartFile panDocument,
            @RequestPart(value = "degreeDocument", required = false) MultipartFile degreeDocument,
            @RequestPart(value = "experienceDocument", required = false) MultipartFile experienceDocument,
            @RequestPart(value = "offerLetter", required = false) MultipartFile offerLetter,
            @RequestPart(value = "profilePhoto", required = false) MultipartFile profilePhoto) {

        try {
            EmployeeRequestDTO dto = objectMapper.readValue(employeeJson, EmployeeRequestDTO.class);

            dto.setAadhaarDocument(aadhaarDocument);
            dto.setPanDocument(panDocument);
            dto.setDegreeDocument(degreeDocument);
            dto.setExperienceDocument(experienceDocument);
            dto.setOfferLetter(offerLetter);
            dto.setProfilePhoto(profilePhoto);

            EmployeeResponseDTO employee = employeeService.updateEmployee(employeePrimeId, dto);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Employee updated successfully");
            response.put("data", employee);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // GET All Employees
    @GetMapping("/get-all-employees")
    public ResponseEntity<Map<String, Object>> getAllEmployees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EmployeeResponseDTO> employees = employeeService.getAllEmployees(
                PageRequest.of(page, size, Sort.by("employeePrimeId").descending()));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", employees.getContent());
        response.put("totalPages", employees.getTotalPages());
        response.put("totalElements", employees.getTotalElements());
        response.put("currentPage", page);
        return ResponseEntity.ok(response);
    }


    //== get all employess ====//
    @GetMapping("/all-employees")
    public ResponseEntity<List<EmployeeSummaryDTO>> getAllEmployees() {
        List<EmployeeSummaryDTO> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // GET Employee by employeePrimeId
    @GetMapping("/get-employee-by-employeePrimeId/{employeePrimeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeById(@PathVariable Long employeePrimeId) {
        EmployeeResponseDTO employee = employeeService.getEmployeeById(employeePrimeId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", employee);
        return ResponseEntity.ok(response);
    }

    // GET Employee by Employee Prime employeePrimeId (EMP001)
    @GetMapping("/get-employee-by-employeeId/{employeeId}")
    public ResponseEntity<Map<String, Object>> getEmployeeByEmployeePrimeId(@PathVariable String employeeId) {
        EmployeeResponseDTO employee = employeeService.getEmployeeByEmployeePrimeId(employeeId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", employee);
        return ResponseEntity.ok(response);
    }

    // GET Employee by Email
    @GetMapping("/get-employee-by-email/{email}")
    public ResponseEntity<Map<String, Object>> getEmployeeByEmail(@PathVariable String email) {
        EmployeeResponseDTO employee = employeeService.getEmployeeByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", employee);
        return ResponseEntity.ok(response);
    }

    // ✅ SEARCH Employees
//    @GetMapping("/search-employees")
//    public ResponseEntity<Map<String, Object>> searchEmployees(
//            @RequestParam String keyword,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Page<EmployeeResponseDTO> employees = employeeService.searchEmployees(keyword, PageRequest.of(page, size));
//        Map<String, Object> response = new HashMap<>();
//        response.put("success", true);
//        response.put("data", employees.getContent());
//        response.put("totalElements", employees.getTotalElements());
//        return ResponseEntity.ok(response);
//    }

    // GET by Department
    @GetMapping("/get-employees-by-department/{department}")
    public ResponseEntity<Map<String, Object>> getEmployeesByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EmployeeResponseDTO> employees = employeeService.getEmployeesByDepartment(department, PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", employees.getContent());
        return ResponseEntity.ok(response);
    }

    // GET by Status
    @GetMapping("/get-employees-by-status/{status}")
    public ResponseEntity<Map<String, Object>> getEmployeesByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<EmployeeResponseDTO> employees = employeeService.getEmployeesByStatus(status, PageRequest.of(page, size));
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", employees.getContent());
        return ResponseEntity.ok(response);
    }

    // UPDATE Status
    @PatchMapping("/update-employee-status/{employeePrimeId}")
    public ResponseEntity<Map<String, Object>> updateEmployeeStatus(
            @PathVariable Long employeePrimeId,
            @RequestParam String status) {

        EmployeeResponseDTO employee = employeeService.updateEmployeeStatus(employeePrimeId, status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Status updated to: " + status);
        response.put("data", employee);
        return ResponseEntity.ok(response);
    }

    // DELETE Employee
    @DeleteMapping("/delete-employee/{employeePrimeId}")
    public ResponseEntity<Map<String, Object>> deleteEmployee(@PathVariable Long employeePrimeId) {
        employeeService.deleteEmployee(employeePrimeId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Employee deleted successfully");
        return ResponseEntity.ok(response);
    }

    // GET Total Count
    @GetMapping("/get-total-count")
    public ResponseEntity<Map<String, Object>> getTotalCount() {
        long count = employeeService.getTotalCount();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("totalEmployees", count);
        return ResponseEntity.ok(response);
    }

    // ✅ BULK DELETE
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteEmployees(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        employeeService.bulkDeleteEmployees(ids);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", ids.size() + " employees deleted successfully");
        return ResponseEntity.ok(response);
    }

    //  EXPORT Employees
    @GetMapping("/export-employees")
    public ResponseEntity<Map<String, Object>> exportEmployees() {
        List<EmployeeResponseDTO> employees = employeeService.getAllEmployeesList();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", employees);
        response.put("totalCount", employees.size());
        return ResponseEntity.ok(response);
    }

    // ========== IMAGE SERVING ENDPOINTS ==========

    @GetMapping(value = "/{employeeId}/aadhaar-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getAadhaarImage(@PathVariable String employeeId) {
        EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        byte[] imageData = employee.getAadhaarDocumentImage();
        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageData);
    }

    @GetMapping(value = "/{employeeId}/pan-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getPanImage(@PathVariable String employeeId) {
        EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        byte[] imageData = employee.getPanDocumentImage();
        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageData);
    }

    @GetMapping(value = "/{employeeId}/profile-image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getProfilePhoto(@PathVariable String employeeId) {
        EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        byte[] imageData = employee.getProfilePhotoImage();
        if (imageData == null || imageData.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageData);
    }

    // ── Utility: detect content type from byte magic bytes ──────────────────
    private MediaType detectMediaType(byte[] data) {
        if (data == null || data.length < 4) return MediaType.APPLICATION_OCTET_STREAM;
        // PDF magic: %PDF → 0x25 0x50 0x44 0x46
        if (data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46) {
            return MediaType.APPLICATION_PDF;
        }
        // JPEG magic: 0xFF 0xD8
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8) {
            return MediaType.IMAGE_JPEG;
        }
        // PNG magic: 0x89 0x50 0x4E 0x47
        if ((data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return MediaType.IMAGE_PNG;
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    @GetMapping("/{employeeId}/degree-image")
    public ResponseEntity<byte[]> getDegreeImage(@PathVariable String employeeId) {
        EmployeeEntity emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        byte[] data = emp.getDegreeDocumentImage();
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(detectMediaType(data))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"degree\"")
                .body(data);
    }

    @GetMapping("/{employeeId}/experience-image")
    public ResponseEntity<byte[]> getExperienceImage(@PathVariable String employeeId) {
        EmployeeEntity emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        byte[] data = emp.getExperienceDocumentImage();
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(detectMediaType(data))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"experience\"")
                .body(data);
    }

    @GetMapping("/{employeeId}/offer-image")
    public ResponseEntity<byte[]> getOfferLetter(@PathVariable String employeeId) {
        EmployeeEntity emp = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        byte[] data = emp.getOfferLetterImage();
        if (data == null || data.length == 0) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .contentType(detectMediaType(data))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"offer-letter\"")
                .body(data);
    }


    @GetMapping("/payroll-list")
    public ResponseEntity<ApiResponse<List<EmployeeForPayrollDTO>>> getEmployeesForPayroll(
            @RequestParam(defaultValue = "all") String department) {

        logger.info("[Employee][GET] Payroll list → dept={}", department);

        List<EmployeeForPayrollDTO> list = employeeService.getEmployeesForPayroll(department);

        String message = list.isEmpty()
                ? "No active employees found for department: " + department
                : "Found " + list.size() + " active employee(s).";

        return ResponseEntity.ok(ApiResponse.success(message, list));
    }

    @GetMapping("/employee-login")
    public ResponseEntity<EmployeeLoginResponseDto> login(
            @RequestParam String employeeId,
            @RequestParam String password) {
        return ResponseEntity.ok(employeeService.login(employeeId, password));
    }

    @PatchMapping("/employee-update-password")
    public ResponseEntity<String> updatePassword(
            @RequestParam String employeeId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            employeeService.updatePassword(employeeId, oldPassword, newPassword);
            return ResponseEntity.ok("Password updated successfully");
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    //======================================================//
    //       register employee for portal credentials       //
    //======================================================//

    @PostMapping("/register")
    public ResponseEntity<RegisteredEmployeeResponseDTO> registerEmployeeForPortal(
            @RequestBody RegisterEmployeeRequestDTO dto) {

        RegisteredEmployeeResponseDTO response = employeeService.registerEmployee(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    /**
     * GET /api/dashboard/{employeePrimeId}
     *
     * Returns the full dashboard payload for the given employee.
     *
     * Path variable is the employee's primary key (Long) — the same
     * value stored as hrms_employee_prime_id in localStorage on the frontend.
     */
    @GetMapping("/dashboard/{employeePrimeId}")
    public ResponseEntity<DashboardDTO> getDashboard(
            @PathVariable Long employeePrimeId) {

        DashboardDTO dto = employeeService.getDashboardData(employeePrimeId);
        return ResponseEntity.ok(dto);
    }


    /**
     * POST /api/employees/bulk-employee
     * Accepts an .xlsx file and bulk-inserts employees.
     * Returns counts of uploaded vs skipped rows with skip reasons.
     */
    @PostMapping(value = "/bulk-employee", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkEmployeeResponse> bulkUpload(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            BulkEmployeeResponse err = new BulkEmployeeResponse();
            err.setUploadedCount(0);
            err.setSkippedCount(0);
            err.getMessage();
            err.setMessage("No file provided. Please attach a valid .xlsx file.");
            return ResponseEntity.badRequest().body(err);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            BulkEmployeeResponse err = new BulkEmployeeResponse();
            err.setMessage("Invalid file type. Only .xlsx or .xls files are accepted.");
            return ResponseEntity.badRequest().body(err);
        }

        BulkEmployeeResponse response = employeeService.processBulkUpload(file);

        // Return 207 Multi-Status when there are partial failures, 200 on full success
        if (response.getSkippedCount() > 0 && response.getUploadedCount() > 0) {
            return ResponseEntity.status(207).body(response);
        } else if (response.getUploadedCount() == 0) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    // Today's birthdays — called right after login to show the popup
    @GetMapping("/todays-birthdays")
    public ResponseEntity<Map<String, Object>> getTodaysBirthdays() {
        List<BirthdayDTO> birthdays = employeeService.getTodaysBirthdays();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", birthdays.size());
        response.put("data", birthdays);
        return ResponseEntity.ok(response);
    }

    // All birthdays for a year — used by Calendar Management page
    @GetMapping("/birthdays")
    public ResponseEntity<Map<String, Object>> getBirthdaysForYear(
            @RequestParam(required = false) Integer year) {
        int resolvedYear = (year != null) ? year : java.time.Year.now().getValue();
        List<BirthdayDTO> birthdays = employeeService.getBirthdaysForYear(resolvedYear);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", birthdays);
        return ResponseEntity.ok(response);
    }
}