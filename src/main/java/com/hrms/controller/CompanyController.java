package com.hrms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.request.CompanyRequestDTO;

import com.hrms.dto.response.CompanyResponseDTO;

import com.hrms.entity.CompanyEntity;
import com.hrms.repository.CompanyRepository;
import com.hrms.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/company")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final CompanyService companyService;
    private final ObjectMapper objectMapper;

    public CompanyController(CompanyRepository companyRepository, CompanyService companyService, ObjectMapper objectMapper) {
        this.companyRepository = companyRepository;
        this.companyService = companyService;
        this.objectMapper = objectMapper;
    }

    @RequestMapping(value = "/profile", method = {RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<CompanyResponseDTO.ApiResponse> saveCompany(@RequestBody CompanyRequestDTO request) {
        try {
            System.out.println("=== SAVE COMPANY ===");
            System.out.println("Request ID: " + request.getId());
            System.out.println("Company Name: " + request.getCompanyName());

            CompanyEntity company;

            if (request.getId() != null && request.getId() > 0) {
                company = companyRepository.findById(request.getId())
                        .orElseThrow(() -> new RuntimeException("Company not found with id: " + request.getId()));
                System.out.println("Updating existing company with ID: " + request.getId());
                System.out.println("Existing logo preserved: " + (company.getLogo() != null ? company.getLogo().length + " bytes" : "none"));
            } else {
                company = new CompanyEntity();
                System.out.println("Creating new company");
            }

            // Set basic fields
            company.setCompanyName(request.getCompanyName());
            company.setGstNumber(request.getGstNumber());
            company.setPanNumber(request.getPanNumber());
            company.setCinNumber(request.getCinNumber());
            company.setRegistrationNumber(request.getRegistrationNumber());
            company.setEmail(request.getEmail());
            company.setPhone(request.getPhone());
            company.setWebsite(request.getWebsite());
            company.setEstablishedYear(request.getEstablishedYear());
            company.setEmployeeCount(request.getEmployeeCount());
            company.setCompanyType(request.getCompanyType());
            company.setIndustryType(request.getIndustryType());
            company.setAddress(request.getAddress());
            company.setDescription(request.getDescription());

            // Set JSON fields (directly as strings)
            if (request.getWorkingDays() != null && !request.getWorkingDays().isEmpty()) {
                company.setWorkingDaysJson(request.getWorkingDays());
            }
            if (request.getBreakTimings() != null && !request.getBreakTimings().isEmpty()) {
                company.setBreakTimingsJson(request.getBreakTimings());
            }
            if (request.getSaturdayPolicy() != null && !request.getSaturdayPolicy().isEmpty()) {
                company.setSaturdayPolicyJson(request.getSaturdayPolicy());
            }
            if (request.getHolidays() != null && !request.getHolidays().isEmpty()) {
                company.setHolidaysJson(request.getHolidays());
            }
            if (request.getDepartments() != null && !request.getDepartments().isEmpty()) {
                company.setDepartmentsJson(request.getDepartments());
            }
            if (request.getDesignations() != null && !request.getDesignations().isEmpty()) {
                company.setDesignationsJson(request.getDesignations());
            }
            if (request.getSocialMedia() != null && !request.getSocialMedia().isEmpty()) {
                company.setSocialMediaJson(request.getSocialMedia());
            }
            if (request.getContactPersons() != null && !request.getContactPersons().isEmpty()) {
                company.setContactPersonsJson(request.getContactPersons());
            }
            if (request.getBankDetails() != null && !request.getBankDetails().isEmpty()) {
                company.setBankDetailsJson(request.getBankDetails());
            }

            CompanyEntity saved = companyRepository.save(company);
            System.out.println("Company saved with ID: " + saved.getId());

            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success("Company saved successfully", convertToResponse(saved)));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logo/{id}")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> uploadLogo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            System.out.println("=== UPLOAD LOGO ===");
            System.out.println("Company ID: " + id);
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            System.out.println("Content type: " + file.getContentType());

            CompanyEntity company = companyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error("File is empty"));
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error("File size exceeds 10MB limit"));
            }

            String contentType = file.getContentType();
            if (contentType == null || (!contentType.equals("image/png") && !contentType.equals("image/jpeg") && !contentType.equals("image/jpg"))) {
                return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error("Only PNG, JPG, JPEG images are allowed. Detected: " + contentType));
            }

            // Read file bytes
            byte[] logoBytes = file.getBytes();
            System.out.println("Read " + logoBytes.length + " bytes from file");

            // Set logo
            company.setLogo(logoBytes);
            company.setLogoContentType(contentType);

            // Save to database
            CompanyEntity saved = companyRepository.save(company);
            System.out.println("Saved to database. Logo in saved entity: " + (saved.getLogo() != null ? saved.getLogo().length + " bytes" : "NULL"));

            // Return relative logo URL
            String logoUrl = "/api/company/logo/" + id;
            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success("Logo uploaded successfully", logoUrl));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error("Failed to read file: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error("Failed to upload logo: " + e.getMessage()));
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> getCompany(@PathVariable Long id) {
        try {
            System.out.println("=== GET COMPANY ===");
            System.out.println("Company ID: " + id);

            CompanyEntity company = companyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

            System.out.println("Company found: " + company.getCompanyName());
            System.out.println("Logo in entity: " + (company.getLogo() != null ? company.getLogo().length + " bytes" : "NULL"));
            System.out.println("Logo content type: " + company.getLogoContentType());

            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success("Company retrieved successfully", convertToResponse(company)));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping(value = "/logo/{id}", produces = {MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_JPEG_VALUE})
    public ResponseEntity<byte[]> getLogoImage(@PathVariable Long id) {
        try {
            System.out.println("=== GET LOGO IMAGE ===");
            System.out.println("Company ID: " + id);

            CompanyEntity company = companyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

            if (company.getLogo() == null || company.getLogo().length == 0) {
                System.out.println("No logo found for company ID: " + id);
                return ResponseEntity.notFound().build();
            }

            System.out.println("Returning logo, size: " + company.getLogo().length + " bytes");
            String contentType = company.getLogoContentType() != null ? company.getLogoContentType() : "image/png";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(company.getLogo());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/profile/{id}")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> deleteCompany(@PathVariable Long id) {
        try {
            if (!companyRepository.existsById(id)) {
                throw new RuntimeException("Company not found with id: " + id);
            }
            companyRepository.deleteById(id);
            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success("Company deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/profiles")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> getAllCompanies() {
        try {
            List<CompanyEntity> companies = companyRepository.findAll();
            List<CompanyResponseDTO> responses = companies.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success("Companies retrieved successfully", responses));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }


    @GetMapping("/by-department")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> getCompaniesByDepartment(
            @RequestParam String department) {
        try {
            List<CompanyResponseDTO> companies = companyService.getCompaniesByDepartment(department);
            String message = companies.isEmpty()
                    ? "No companies found with department: " + department
                    : "Companies retrieved successfully";
            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success(message, companies));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/by-designation")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> getCompaniesByDesignation(
            @RequestParam String designation) {
        try {
            List<CompanyResponseDTO> companies = companyService.getCompaniesByDesignation(designation);
            String message = companies.isEmpty()
                    ? "No companies found with designation: " + designation
                    : "Companies retrieved successfully";
            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success(message, companies));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }


    @GetMapping("/departments")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> getAllDepartments() {
        try {
            List<String> departments = companyService.getAllDepartments();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalCount", departments.size());
            result.put("departments", departments);
            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success(
                    "Departments retrieved successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/designations")
    public ResponseEntity<CompanyResponseDTO.ApiResponse> getAllDesignations() {
        try {
            List<String> designations = companyService.getAllDesignations();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("totalCount", designations.size());
            result.put("designations", designations);
            return ResponseEntity.ok(CompanyResponseDTO.ApiResponse.success(
                    "Designations retrieved successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(CompanyResponseDTO.ApiResponse.error(e.getMessage()));
        }
    }


    private CompanyResponseDTO convertToResponse(CompanyEntity company) {
        CompanyResponseDTO response = new CompanyResponseDTO();
        response.setId(company.getId());
        response.setCompanyName(company.getCompanyName());
        response.setGstNumber(company.getGstNumber());
        response.setPanNumber(company.getPanNumber());
        response.setCinNumber(company.getCinNumber());
        response.setRegistrationNumber(company.getRegistrationNumber());
        response.setEmail(company.getEmail());
        response.setPhone(company.getPhone());
        response.setWebsite(company.getWebsite());
        response.setEstablishedYear(company.getEstablishedYear());
        response.setEmployeeCount(company.getEmployeeCount());
        response.setCompanyType(company.getCompanyType());
        response.setIndustryType(company.getIndustryType());
        response.setAddress(company.getAddress());
        response.setDescription(company.getDescription());

        // Generate relative logo URL (without base URL)
        if (company.getLogo() != null && company.getLogo().length > 0) {
            String logoUrl = "/api/company/logo/" + company.getId();
            response.setLogoUrl(logoUrl);
            response.setLogoContentType(company.getLogoContentType());
        }

        response.setWorkingDaysJson(company.getWorkingDaysJson());
        response.setBreakTimingsJson(company.getBreakTimingsJson());
        response.setSaturdayPolicyJson(company.getSaturdayPolicyJson());
        response.setHolidaysJson(company.getHolidaysJson());
        response.setDepartmentsJson(company.getDepartmentsJson());
        response.setDesignationsJson(company.getDesignationsJson());
        response.setSocialMediaJson(company.getSocialMediaJson());
        response.setContactPersonsJson(company.getContactPersonsJson());
        response.setBankDetailsJson(company.getBankDetailsJson());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }
}

