package com.hrms.service.serviceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.config.BcryptEncoderConfig;
import com.hrms.dto.request.DashboardDTO;
import com.hrms.dto.request.EmployeeRequestDTO;
import com.hrms.dto.request.RegisterEmployeeRequestDTO;
import com.hrms.dto.response.*;
import com.hrms.entity.AttendanceEntity;
import com.hrms.entity.EmployeeEntity;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.EmployeeLeaveBalanceRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.service.EmailService;
import com.hrms.service.EmployeeService;
import com.hrms.service.LeaveRequestService;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeLeaveBalanceRepository leaveBalanceRepository;
    private final EmailService emailService;
    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final BcryptEncoderConfig passwordEncoder;
    private final LeaveRequestService leaveRequestService;

    public EmployeeServiceImpl(EmployeeLeaveBalanceRepository leaveBalanceRepository,
                               EmailService emailService, AttendanceRepository attendanceRepository, EmployeeRepository employeeRepository, BcryptEncoderConfig passwordEncoder, LeaveRequestService leaveRequestService) {
        this.leaveBalanceRepository = leaveBalanceRepository;
        this.emailService = emailService;
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.leaveRequestService = leaveRequestService;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Helper method to parse date from string
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method to convert MultipartFile to byte[]
    private byte[] convertToBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return file.getBytes();
        } catch (IOException e) {
            logger.error("Failed to convert file to bytes", e);
            return null;
        }
    }

    // Helper method to generate image URL
    private String generateImageUrl(String employeeId, String documentType) {
        return "/api/employees/" + employeeId + "/" + documentType + "-image";
    }



    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO dto) {

        if (employeeRepository.existsByPersonalEmail(dto.getPersonalEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getPersonalEmail());
        }

        // Generate Employee Prime employeePrimeId
//        long count = employeeRepository.count() + 1;

        // Auto-generate employeeId based on max existing series number
        int nextSeq = employeeRepository.findTopEmployeeIdBySeries()
                .map(id -> Integer.parseInt(id.substring(7)) + 1)
                .orElse(1);

        String employeeId = String.format("EMPKMS%02d", nextSeq);

        EmployeeEntity employee = new EmployeeEntity();

        // Basic Info
        employee.setEmployeeId(employeeId);
        employee.setFirstName(dto.getFirstName());
        employee.setMiddleName(dto.getMiddleName());
        employee.setLastName(dto.getLastName());
        employee.setFullName(dto.getFirstName() + " " +
                (dto.getMiddleName() != null ? dto.getMiddleName() + " " : "") +
                dto.getLastName());

        employee.setDateOfBirth(parseDate(dto.getDateOfBirth()));
        employee.setGender(dto.getGender());
        employee.setMaritalStatus(dto.getMaritalStatus());
        employee.setBloodGroup(dto.getBloodGroup());
        employee.setPanNumber(dto.getPanNumber());
        employee.setAadhaarNumber(dto.getAadhaarNumber());
        employee.setNationality(dto.getNationality());
        employee.setReligion(dto.getReligion());
        employee.setLinkedinProfile(dto.getLinkedinProfile());
        employee.setFatherSpouseName(dto.getFatherSpouseName());

        employee.setPassword(passwordEncoder.encode(dto.getPassword()));


        // PWD Details
        employee.setIsPhysicallyChallenged(dto.getIsPhysicallyChallenged());
        employee.setDisabilityType(dto.getDisabilityType());
        employee.setDisabilityPercentage(dto.getDisabilityPercentage());
        employee.setCertificateNumber(dto.getCertificateNumber());

        // Job Details
        employee.setDepartment(dto.getDepartment());
        employee.setSubDepartment(dto.getSubDepartment());
        employee.setDesignation(dto.getDesignation());
        employee.setEmployeeGrade(dto.getEmployeeGrade());
        employee.setEmploymentType(dto.getEmploymentType());
        employee.setJoiningDate(parseDate(dto.getJoiningDate()));
        employee.setProbationEndDate(parseDate(dto.getProbationEndDate()));
        employee.setReportingManager(dto.getReportingManager());
        employee.setHrBusinessPartner(dto.getHrBusinessPartner());
        employee.setWorkLocation(dto.getWorkLocation());
        employee.setBasicSalary(dto.getBasicSalary());
        employee.setShift(dto.getShift());
        employee.setCostCentre(dto.getCostCentre());
        employee.setStatus("ACTIVE");
        employee.setProfileStatus(dto.getProfileStatus());

        // Bank Details
        employee.setBankName(dto.getBankName());
        employee.setAccountNumber(dto.getAccountNumber());
        employee.setIfscCode(dto.getIfscCode());

        // Contact
        employee.setPersonalEmail(dto.getPersonalEmail());
        employee.setWorkEmail(dto.getWorkEmail());
        employee.setMobileNumber(dto.getMobileNumber());
        employee.setAlternateNumber(dto.getAlternateNumber());

        // Address
        employee.setCurrentStreet(dto.getCurrentStreet());
        employee.setCurrentCity(dto.getCurrentCity());
        employee.setCurrentState(dto.getCurrentState());
        employee.setCurrentPincode(dto.getCurrentPincode());
        employee.setCurrentCountry(dto.getCurrentCountry());
        employee.setPermanentStreet(dto.getPermanentStreet());
        employee.setPermanentCity(dto.getPermanentCity());
        employee.setPermanentState(dto.getPermanentState());
        employee.setPermanentPincode(dto.getPermanentPincode());
        employee.setPermanentCountry(dto.getPermanentCountry());

        // Emergency
        employee.setEmergencyName(dto.getEmergencyName());
        employee.setEmergencyRelationship(dto.getEmergencyRelationship());
        employee.setEmergencyPhone(dto.getEmergencyPhone());

        // Education, Family, Experience
        employee.setEducation(dto.getEducation());
        employee.setFamily(dto.getFamily());
        employee.setWorkExperience(dto.getWorkExperience());

        // Save Images as BLOB in Database
        employee.setAadhaarDocumentImage(convertToBytes(dto.getAadhaarDocument()));
        employee.setPanDocumentImage(convertToBytes(dto.getPanDocument()));
        employee.setDegreeDocumentImage(convertToBytes(dto.getDegreeDocument()));
        employee.setExperienceDocumentImage(convertToBytes(dto.getExperienceDocument()));
        employee.setOfferLetterImage(convertToBytes(dto.getOfferLetter()));
        employee.setProfilePhotoImage(convertToBytes(dto.getProfilePhoto()));

        employee.setCreatedAt(LocalDate.now());

        EmployeeEntity saved = employeeRepository.save(employee);
        return convertToResponseDTO(saved);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long employeePrimeId, EmployeeRequestDTO dto) {
        EmployeeEntity employee = employeeRepository.findById(employeePrimeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with employeePrimeId: " + employeePrimeId));

        // Update all fields
        employee.setFirstName(dto.getFirstName());
        employee.setMiddleName(dto.getMiddleName());
        employee.setLastName(dto.getLastName());
        employee.setFullName(dto.getFirstName() + " " +
                (dto.getMiddleName() != null ? dto.getMiddleName() + " " : "") +
                dto.getLastName());
        employee.setDateOfBirth(parseDate(dto.getDateOfBirth()));
        employee.setGender(dto.getGender());
        employee.setMaritalStatus(dto.getMaritalStatus());
        employee.setBloodGroup(dto.getBloodGroup());

        // WITH these:
        employee.setAadhaarNumber(StringUtils.hasText(dto.getAadhaarNumber()) ? dto.getAadhaarNumber() : null);
        employee.setPanNumber(StringUtils.hasText(dto.getPanNumber()) ? dto.getPanNumber() : null);

        employee.setNationality(dto.getNationality());
        employee.setReligion(dto.getReligion());
        employee.setLinkedinProfile(dto.getLinkedinProfile());
        employee.setFatherSpouseName(dto.getFatherSpouseName());
        employee.setIsPhysicallyChallenged(dto.getIsPhysicallyChallenged());
        employee.setDisabilityType(dto.getDisabilityType());
        employee.setDisabilityPercentage(dto.getDisabilityPercentage());
        employee.setCertificateNumber(dto.getCertificateNumber());
        employee.setDepartment(dto.getDepartment());
        employee.setSubDepartment(dto.getSubDepartment());
        employee.setDesignation(dto.getDesignation());
        employee.setEmployeeGrade(dto.getEmployeeGrade());
        employee.setEmploymentType(dto.getEmploymentType());
        employee.setJoiningDate(parseDate(dto.getJoiningDate()));
        employee.setProbationEndDate(parseDate(dto.getProbationEndDate()));
        employee.setReportingManager(dto.getReportingManager());
        employee.setHrBusinessPartner(dto.getHrBusinessPartner());
        employee.setWorkLocation(dto.getWorkLocation());
        employee.setBasicSalary(dto.getBasicSalary());
        employee.setShift(dto.getShift());
        employee.setCostCentre(dto.getCostCentre());
        employee.setBankName(dto.getBankName());
        employee.setAccountNumber(dto.getAccountNumber());
        employee.setIfscCode(dto.getIfscCode());

        employee.setPersonalEmail(dto.getPersonalEmail());


        employee.setWorkEmail(StringUtils.hasText(dto.getWorkEmail()) ? dto.getWorkEmail() : null);

        employee.setMobileNumber(dto.getMobileNumber());
        employee.setAlternateNumber(dto.getAlternateNumber());
        employee.setCurrentStreet(dto.getCurrentStreet());
        employee.setCurrentCity(dto.getCurrentCity());
        employee.setCurrentState(dto.getCurrentState());
        employee.setCurrentPincode(dto.getCurrentPincode());
        employee.setCurrentCountry(dto.getCurrentCountry());
        employee.setPermanentStreet(dto.getPermanentStreet());
        employee.setPermanentCity(dto.getPermanentCity());
        employee.setPermanentState(dto.getPermanentState());
        employee.setPermanentPincode(dto.getPermanentPincode());
        employee.setPermanentCountry(dto.getPermanentCountry());
        employee.setEmergencyName(dto.getEmergencyName());
        employee.setEmergencyRelationship(dto.getEmergencyRelationship());
        employee.setEmergencyPhone(dto.getEmergencyPhone());
        employee.setEducation(dto.getEducation());
        employee.setFamily(dto.getFamily());
        employee.setWorkExperience(dto.getWorkExperience());

        employee.setStatus(dto.getStatus());

        //profile status
        employee.setProfileStatus(dto.getProfileStatus());

        // Update images if new files provided
        if (dto.getAadhaarDocument() != null && !dto.getAadhaarDocument().isEmpty()) {
            employee.setAadhaarDocumentImage(convertToBytes(dto.getAadhaarDocument()));
        }
        if (dto.getPanDocument() != null && !dto.getPanDocument().isEmpty()) {
            employee.setPanDocumentImage(convertToBytes(dto.getPanDocument()));
        }
        if (dto.getDegreeDocument() != null && !dto.getDegreeDocument().isEmpty()) {
            employee.setDegreeDocumentImage(convertToBytes(dto.getDegreeDocument()));
        }
        if (dto.getExperienceDocument() != null && !dto.getExperienceDocument().isEmpty()) {
            employee.setExperienceDocumentImage(convertToBytes(dto.getExperienceDocument()));
        }
        if (dto.getOfferLetter() != null && !dto.getOfferLetter().isEmpty()) {
            employee.setOfferLetterImage(convertToBytes(dto.getOfferLetter()));
        }
        if (dto.getProfilePhoto() != null && !dto.getProfilePhoto().isEmpty()) {
            employee.setProfilePhotoImage(convertToBytes(dto.getProfilePhoto()));
        }

        employee.setUpdatedAt(LocalDate.now());

        EmployeeEntity updated = employeeRepository.save(employee);
        return convertToResponseDTO(updated);
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long employeePrimeId) {
        EmployeeEntity employee = employeeRepository.findById(employeePrimeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToResponseDTO(employee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByEmployeePrimeId(String employeeId) {
        EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return convertToResponseDTO(employee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByEmail(String email) {
        EmployeeEntity employee = employeeRepository.findByPersonalEmail(email)
                .orElseThrow(() -> new RuntimeException("Employee not found with email: " + email));
        return convertToResponseDTO(employee);
    }

    @Override
    public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::convertToResponseDTO);
    }

    @Override
    public Page<EmployeeResponseDTO> getEmployeesByDepartment(String department, Pageable pageable) {
        return employeeRepository.findByDepartment(department, pageable).map(this::convertToResponseDTO);
    }

    @Override
    public Page<EmployeeResponseDTO> getEmployeesByStatus(String status, Pageable pageable) {
        return employeeRepository.findByStatus(status, pageable).map(this::convertToResponseDTO);
    }


    @Override
    public List<EmployeeResponseDTO> getAllEmployeesList() {
        return employeeRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteEmployee(Long employeePrimeId) {
        EmployeeEntity employee = employeeRepository.findById(employeePrimeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setStatus("Inactive");
        employee.setUpdatedAt(LocalDate.now());
        employeeRepository.save(employee);
    }

    @Override
    @Transactional
    public void bulkDeleteEmployees(List<Long> ids) {
        List<EmployeeEntity> employees = employeeRepository.findAllById(ids);
        for (EmployeeEntity employee : employees) {
            employee.setStatus("Inactive");
            employee.setUpdatedAt(LocalDate.now());
        }
        employeeRepository.saveAll(employees);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployeeStatus(Long employeePrimeId, String status) {
        EmployeeEntity employee = employeeRepository.findById(employeePrimeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setStatus(status);
        employee.setUpdatedAt(LocalDate.now());
        return convertToResponseDTO(employeeRepository.save(employee));
    }

    @Override
    public long getTotalCount() {
        return employeeRepository.count();
    }

    // Convert to Response DTO
    private EmployeeResponseDTO convertToResponseDTO(EmployeeEntity emp) {
        EmployeeResponseDTO dto = new EmployeeResponseDTO();

        dto.setEmployeePrimeId(emp.getEmployeePrimeId());
        dto.setEmployeeId(emp.getEmployeeId());
        dto.setFirstName(emp.getFirstName());
        dto.setMiddleName(emp.getMiddleName());
        dto.setLastName(emp.getLastName());
        dto.setFullName(emp.getFullName());
        dto.setDateOfBirth(emp.getDateOfBirth());
        dto.setGender(emp.getGender());
        dto.setMaritalStatus(emp.getMaritalStatus());
        dto.setBloodGroup(emp.getBloodGroup());
        dto.setPanNumber(emp.getPanNumber());
        dto.setAadhaarNumber(emp.getAadhaarNumber());
        dto.setNationality(emp.getNationality());
        dto.setReligion(emp.getReligion());
        dto.setLinkedinProfile(emp.getLinkedinProfile());
        dto.setFatherSpouseName(emp.getFatherSpouseName());
        dto.setIsPhysicallyChallenged(emp.getIsPhysicallyChallenged());
        dto.setDisabilityType(emp.getDisabilityType());
        dto.setDisabilityPercentage(emp.getDisabilityPercentage());
        dto.setCertificateNumber(emp.getCertificateNumber());
        dto.setDepartment(emp.getDepartment());
        dto.setSubDepartment(emp.getSubDepartment());
        dto.setDesignation(emp.getDesignation());
        dto.setEmployeeGrade(emp.getEmployeeGrade());
        dto.setEmploymentType(emp.getEmploymentType());
        dto.setJoiningDate(emp.getJoiningDate());
        dto.setProbationEndDate(emp.getProbationEndDate());
        dto.setReportingManager(emp.getReportingManager());
        dto.setHrBusinessPartner(emp.getHrBusinessPartner());
        dto.setWorkLocation(emp.getWorkLocation());
        dto.setBasicSalary(emp.getBasicSalary());
        dto.setShift(emp.getShift());
        dto.setCostCentre(emp.getCostCentre());
        dto.setStatus(emp.getStatus());
        dto.setBankName(emp.getBankName());
        dto.setAccountNumber(emp.getAccountNumber());
        dto.setIfscCode(emp.getIfscCode());
        dto.setPersonalEmail(emp.getPersonalEmail());
        dto.setWorkEmail(emp.getWorkEmail());
        dto.setMobileNumber(emp.getMobileNumber());
        dto.setAlternateNumber(emp.getAlternateNumber());
        dto.setCurrentStreet(emp.getCurrentStreet());
        dto.setCurrentCity(emp.getCurrentCity());
        dto.setCurrentState(emp.getCurrentState());
        dto.setCurrentPincode(emp.getCurrentPincode());
        dto.setCurrentCountry(emp.getCurrentCountry());
        dto.setPermanentStreet(emp.getPermanentStreet());
        dto.setPermanentCity(emp.getPermanentCity());
        dto.setPermanentState(emp.getPermanentState());
        dto.setPermanentPincode(emp.getPermanentPincode());
        dto.setPermanentCountry(emp.getPermanentCountry());
        dto.setEmergencyName(emp.getEmergencyName());
        dto.setEmergencyRelationship(emp.getEmergencyRelationship());
        dto.setEmergencyPhone(emp.getEmergencyPhone());
        dto.setEducation(emp.getEducation());
        dto.setFamily(emp.getFamily());
        dto.setWorkExperience(emp.getWorkExperience());

        dto.setProfileStatus(emp.getProfileStatus());

        // Set Image URLs (not Base64)
        if (emp.getEmployeeId() != null) {
            if (emp.getAadhaarDocumentImage() != null && emp.getAadhaarDocumentImage().length > 0)
                dto.setAadhaarDocumentUrl(generateImageUrl(emp.getEmployeeId(), "aadhaar"));

            if (emp.getPanDocumentImage() != null && emp.getPanDocumentImage().length > 0)
                dto.setPanDocumentUrl(generateImageUrl(emp.getEmployeeId(), "pan"));

            if (emp.getDegreeDocumentImage() != null && emp.getDegreeDocumentImage().length > 0)
                dto.setDegreeDocumentUrl(generateImageUrl(emp.getEmployeeId(), "degree"));

            if (emp.getExperienceDocumentImage() != null && emp.getExperienceDocumentImage().length > 0)
                dto.setExperienceDocumentUrl(generateImageUrl(emp.getEmployeeId(), "experience"));

            if (emp.getOfferLetterImage() != null && emp.getOfferLetterImage().length > 0)
                dto.setOfferLetterUrl(generateImageUrl(emp.getEmployeeId(), "offer"));

            if (emp.getProfilePhotoImage() != null && emp.getProfilePhotoImage().length > 0)
                dto.setProfilePhotoUrl(generateImageUrl(emp.getEmployeeId(), "profile"));
        }


        dto.setCreatedAt(emp.getCreatedAt());
        dto.setUpdatedAt(emp.getUpdatedAt());

        return dto;
    }

    @Override
    public List<EmployeeSummaryDTO> getAllEmployees() {
        List<EmployeeEntity> employees = employeeRepository.findAll();
        return convertToSummaryDTO(employees);
    }
    private List<EmployeeSummaryDTO> convertToSummaryDTO(List<EmployeeEntity> employees) {
        return employees.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private EmployeeSummaryDTO convertToDTO(EmployeeEntity employee) {

        EmployeeSummaryDTO dto = new EmployeeSummaryDTO();

        dto.setEmployeePrimeId(employee.getEmployeePrimeId());
        dto.setEmployeeId(employee.getEmployeeId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setDepartment(employee.getDepartment());
        dto.setGender(employee.getGender());
        dto.setEmploymentType(employee.getEmploymentType());
        dto.setJoiningDate(employee.getJoiningDate());
        return dto;
    }


    @Override
    @Transactional
    public List<EmployeeForPayrollDTO> getEmployeesForPayroll(String department) {
        logger.info("[Employee] Fetching employees for payroll dropdown → dept={}", department);

        boolean filterByDept = department != null
                && !department.isBlank()
                && !department.equalsIgnoreCase("all");

        List<EmployeeEntity> employees = filterByDept
                ? employeeRepository.findActiveByDepartment(department)
                : employeeRepository.findAllActive();

        logger.info("[Employee] Found {} active employee(s) for dept={}", employees.size(), department);

        return employees.stream().map(e -> new EmployeeForPayrollDTO(
                e.getEmployeePrimeId(),
                e.getEmployeeId(),
                e.getFullName() != null
                        ? e.getFullName()
                        : (e.getFirstName() + " " + e.getLastName()).trim(),
                e.getDesignation(),
                e.getDepartment(),
                e.getBasicSalary()
        )).collect(Collectors.toList());
    }


    public EmployeeLoginResponseDto login(String employeeId, String password) {
        EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + employeeId));

        if (!passwordEncoder.matches(password, employee.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new EmployeeLoginResponseDto(
                employee.getEmployeePrimeId(),
                employee.getEmployeeId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getWorkEmail()
        );
    }

    public void updatePassword(String employeeId, String oldPassword, String newPassword) {
        EmployeeEntity employee = employeeRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new RuntimeException("No employee found with ID: " + employeeId));

        if (!passwordEncoder.matches(oldPassword, employee.getPassword())) {
            throw new RuntimeException("Old password does not match");
        }

        employee.setPassword(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }


    //=========================================================//
    //           register employee  for portal credentials     //
    //=========================================================//

    private String generateRandomPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@#$_-";           // You can add more if needed

        String allChars = upper + lower + digits + special;
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each type
        password.append(upper.charAt((int) (Math.random() * upper.length())));
        password.append(lower.charAt((int) (Math.random() * lower.length())));
        password.append(digits.charAt((int) (Math.random() * digits.length())));
        password.append(special.charAt((int) (Math.random() * special.length())));

        // Fill remaining characters
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }

        // Shuffle the password
        char[] chars = password.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = (int) (Math.random() * (i + 1));
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }

        return new String(chars);
    }

    public RegisteredEmployeeResponseDTO registerEmployee(RegisterEmployeeRequestDTO dto) {

        // ADD THIS CHECK
        if (employeeRepository.existsByPersonalEmail(dto.getPersonalEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getPersonalEmail());
        }

        // Auto-generate employeeId
        // long count = employeeRepository.count();

        // Auto-generate employeeId based on max existing series number
        int nextSeq = employeeRepository.findTopEmployeeIdBySeries()
                .map(id -> Integer.parseInt(id.substring(7)) + 1)
                .orElse(1);

        String employeeId = String.format("EMPKMS%02d", nextSeq);

        // Generate random password (8 chars: letters + numbers + special char)
        String rawPassword = generateRandomPassword(8);

        EmployeeEntity employee = new EmployeeEntity();

        employee.setEmployeeId(employeeId);
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setFullName(dto.getFirstName() + " " + dto.getLastName());
        employee.setPassword(passwordEncoder.encode(rawPassword));   // Save encoded password
        employee.setPersonalEmail(dto.getPersonalEmail());           // ← NEW
        employee.setProfileStatus("INCOMPLETE");
        employee.setStatus("ACTIVE");
        employee.setCreatedAt(LocalDate.now());
        employee.setUpdatedAt(LocalDate.now());
        employee.setDepartment(dto.getDepartment());
        employee.setDesignation(dto.getDesignation());

        EmployeeEntity saved = employeeRepository.save(employee);

        // Send credentials via email
        try {
            emailService.sendEmployeeCredentials(
                    dto.getPersonalEmail(),
                    employeeId,
                    rawPassword
            );
        } catch (Exception e) {
            logger.error("Failed to send credentials email to {}", dto.getPersonalEmail(), e);
            // Don't fail registration if email fails (optional: you can throw if critical)
        }

        return new RegisteredEmployeeResponseDTO(
                saved.getEmployeePrimeId(),
                saved.getEmployeeId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getStatus(),
                saved.getDepartment(),
                saved.getDesignation()
        );
    }




    // ─────────────────────────────────────────────────────────────────────────
    @Override
    public DashboardDTO getDashboardData(Long employeePrimeId) {

        LocalDate today = LocalDate.now();
        int       year  = today.getYear();

        // ── Resolve String employeeId from Long employeePrimeId ──────────────────
        String empId = employeeRepository
                .findByEmployeePrimeId(employeePrimeId)
                .orElseThrow(() -> new RuntimeException(
                        "Employee not found for id: " + employeePrimeId))
                .getEmployeeId();

        // ── 1. Today's stat card ──────────────────────────────────────────────────
        Optional<AttendanceEntity> todayRecord =
                attendanceRepository.findByEmployeeEmployeePrimeIdAndAttendanceDate(
                        employeePrimeId, today);

        String todayStatus = todayRecord
                .map(AttendanceEntity::getStatus)
                .orElse("Not Marked");

        Double todayHours = todayRecord
                .map(a -> a.getTotalHours() != null ? a.getTotalHours() : 0.0)
                .orElse(0.0);

        // ── 2. Leave balance — reuse existing LeaveRequestService logic ───────────
        // getEmployeeBalance() already handles:
        //   - applicable leave types per employee (incl. Maternity Leave eligibility)
        //   - getOrCreateBalanceRecord (creates missing rows if needed)
        //   - correct totalAllotted / totalUsed / totalRemaining computation
        LeaveBalanceDTO leaveBalance = leaveRequestService.getEmployeeBalance(empId, year);

        int totalAllotted  = leaveBalance.getTotalAllotted();
        int totalUsed      = leaveBalance.getTotalUsed();
        int totalRemaining = leaveBalance.getTotalRemaining();

        // ── 3. Weekly chart (Mon → Sat of current week) ───────────────────────────
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd   = weekStart.plusDays(5); // Saturday

        List<AttendanceEntity> weekRecords =
                attendanceRepository.findByEmployeeEmployeePrimeIdAndAttendanceDateBetween(
                        employeePrimeId, weekStart, weekEnd);

        Map<LocalDate, Double> weekMap = weekRecords.stream()
                .collect(Collectors.toMap(
                        AttendanceEntity::getAttendanceDate,
                        a -> a.getTotalHours() != null ? a.getTotalHours() : 0.0,
                        (a, b) -> a
                ));

        List<String> weeklyLabels = new ArrayList<>();
        List<Double> weeklyHours  = new ArrayList<>();

        for (int i = 0; i <= 5; i++) {
            LocalDate day = weekStart.plusDays(i);
            weeklyLabels.add(day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
            weeklyHours.add(weekMap.getOrDefault(day, 0.0));
        }

        // ── 4. Monthly chart (every day of current month) ─────────────────────────
        List<AttendanceEntity> monthRecords =
                attendanceRepository.findByEmployeeAndMonth(
                        employeePrimeId, year, today.getMonthValue());

        Map<Integer, Double> monthMap = monthRecords.stream()
                .collect(Collectors.toMap(
                        a -> a.getAttendanceDate().getDayOfMonth(),
                        a -> a.getTotalHours() != null ? a.getTotalHours() : 0.0,
                        (a, b) -> a
                ));

        int daysInMonth = today.lengthOfMonth();
        List<String> monthlyLabels = new ArrayList<>();
        List<Double> monthlyHours  = new ArrayList<>();

        for (int d = 1; d <= daysInMonth; d++) {
            monthlyLabels.add(String.valueOf(d));
            monthlyHours.add(monthMap.getOrDefault(d, 0.0));
        }

        // ── 5. Assemble ───────────────────────────────────────────────────────────
        return new DashboardDTO(
                todayStatus,
                todayHours,
                totalAllotted,
                totalUsed,
                totalRemaining,
                weeklyLabels,
                weeklyHours,
                monthlyLabels,
                monthlyHours
        );
    }

    //=========================================================//
    //                  Bulk Upload API                        //
    //=========================================================//

    private static final int BATCH_SIZE = 50;

    // Scanned at runtime — not hardcoded
    // Column indices resolved dynamically from header row
    private static final String[] EXPECTED_HEADERS = {
            "first name", "middle name", "last name", "gender",
            "department", "designation", "employment type", "basic salary",
            "personal email", "work email", "mobile number", "alternate number",
            "bank name", "account number", "ifsc code"
    };

    private static final Set<String> VALID_DEPARTMENTS =
            Set.of("L1", "L2", "L3", "L4", "L5", "L6");

    private static final Map<String, Set<String>> DEPT_DESIGNATION_MAP = Map.of(
            "L1", Set.of("CEO", "DIRECTOR"),
            "L2", Set.of("VP", "BUSINESS HEAD"),
            "L3", Set.of("MANAGER"),
            "L4", Set.of("SENIOR ENGINEER", "TEAM LEAD"),
            "L5", Set.of("SUPERVISORY", "SR. EXECUTIVE"),
            "L6", Set.of("ENGINEER", "TECHNICIAN", "EXECUTIVE")
    );


    @Override
    @Transactional
    public BulkEmployeeResponse processBulkUpload(MultipartFile file) {
        BulkEmployeeResponse response = new BulkEmployeeResponse();
        List<String> skippedReasons = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                response.setMessage("Excel file is empty or has no sheets.");
                return response;
            }

            DataFormatter formatter = new DataFormatter();

            // ── Step 1: Auto-detect header row (scan first 5 rows) ─────────────
            int headerRowIdx = findHeaderRow(sheet, formatter);
            if (headerRowIdx == -1) {
                response.setMessage(
                        "Could not find a valid header row in the first 5 rows. " +
                                "Ensure your file contains column headers like 'First Name', 'Last Name', " +
                                "'Personal Email', 'Department', 'Designation'.");
                return response;
            }

            int dataStartRowIdx = headerRowIdx + 1;
            int lastRow = sheet.getLastRowNum();

            logger.info("Header row detected at index {} (Excel row {}). Data starts at index {}.",
                    headerRowIdx, headerRowIdx + 1, dataStartRowIdx);

            if (lastRow < dataStartRowIdx) {
                response.setMessage("No data rows found. Please fill in employee details below the header row.");
                response.setUploadedCount(0);
                response.setSkippedCount(0);
                return response;
            }

            // ── Step 2: Build dynamic column index map from header row ──────────
            Map<String, Integer> colMap = buildColumnMap(sheet.getRow(headerRowIdx), formatter);
            logger.debug("Column map: {}", colMap);

            List<String> missingCols = validateRequiredColumns(colMap);
            if (!missingCols.isEmpty()) {
                response.setMessage("Missing required column(s) in header: " + String.join(", ", missingCols) +
                        ". Please use the provided template.");
                return response;
            }

            // ── Step 3: Pre-fetch existing emails + compute ID base ─────────────
            // employeeRepository.count() called ONCE here — not per row.
            // employeeIdCounter increments in-memory so every row in the batch
            // gets a unique EMP#### without re-querying the DB.
            // This mirrors createEmployee's "count + 1" logic but is batch-safe.
            Set<String> existingPersonalEmails = employeeRepository.findAllPersonalEmails()
                    .stream().map(String::toLowerCase).collect(Collectors.toSet());
            Set<String> existingWorkEmails = employeeRepository.findAllWorkEmails()
                    .stream().filter(Objects::nonNull).map(String::toLowerCase).collect(Collectors.toSet());

            // Pre-fetch all existing employeeIds to guarantee uniqueness
            Set<String> existingEmployeeIds = employeeRepository.findAllEmployeeIds()
                    .stream().collect(Collectors.toSet());


            // Base count: next ID = currentCount + 1, incremented per valid row
//            long employeeIdCounter = employeeRepository.count() + 1;

            // Base: next ID = (max existing series number) + 1, incremented per valid row
            long employeeIdCounter = employeeRepository.findTopEmployeeIdBySeries()
                    .map(id -> Long.parseLong(id.substring(7)) + 1)
                    .orElse(1L);

            Set<String> batchPersonalEmails = new HashSet<>();
            Set<String> batchWorkEmails = new HashSet<>();

            List<EmployeeEntity> validEntities = new ArrayList<>();
            int uploadedCount = 0;
            int skippedCount = 0;

            // ── Step 4: Process data rows ────────────────────────────────────────
            for (int rowIdx = dataStartRowIdx; rowIdx <= lastRow; rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                int excelRowNum = rowIdx + 1;

                // Skip null rows and genuinely empty rows
                if (row == null || isRowBlank(row, formatter, colMap)) {
                    continue;
                }

                // Parse cells using dynamic column map
                String firstName      = cell(row, colMap, "first name", formatter);
                String middleName     = cell(row, colMap, "middle name", formatter);
                String lastName       = cell(row, colMap, "last name", formatter);
                String gender         = cell(row, colMap, "gender", formatter);
                String department     = cell(row, colMap, "department", formatter);
                String designation    = cell(row, colMap, "designation", formatter);
                String employmentType = cell(row, colMap, "employment type", formatter);
                Double basicSalary    = parseDouble(row, colMap, "basic salary", formatter);
                String personalEmail  = cell(row, colMap, "personal email", formatter);
                String workEmail      = cell(row, colMap, "work email", formatter);
                String mobile         = cell(row, colMap, "mobile number", formatter);
                String alternate      = cell(row, colMap, "alternate number", formatter);
                String bankName       = cell(row, colMap, "bank name", formatter);
                String accountNumber  = cell(row, colMap, "account number", formatter);
                String ifscCode       = cell(row, colMap, "ifsc code", formatter);

                List<String> rowErrors = new ArrayList<>();

                // ── Mandatory fields ─────────────────────────────────────────────
                if (firstName.isEmpty())     rowErrors.add("First Name is required");
                if (lastName.isEmpty())      rowErrors.add("Last Name is required");
                if (personalEmail.isEmpty()) rowErrors.add("Personal Email is required");
                if (department.isEmpty())    rowErrors.add("Department is required");
                if (designation.isEmpty())   rowErrors.add("Designation is required");

                // ── Department validation ────────────────────────────────────────
                String deptUpper = department.toUpperCase();
                if (!department.isEmpty() && !VALID_DEPARTMENTS.contains(deptUpper)) {
                    rowErrors.add("Invalid Department '" + department + "'. Must be one of: L1, L2, L3, L4, L5, L6");
                }

                // ── Designation cross-validation ─────────────────────────────────
                if (!department.isEmpty() && !designation.isEmpty()
                        && VALID_DEPARTMENTS.contains(deptUpper)) {
                    Set<String> allowed = DEPT_DESIGNATION_MAP.get(deptUpper);
                    if (allowed != null && !allowed.contains(designation.toUpperCase())) {
                        rowErrors.add("Designation '" + designation + "' is not valid for Department '" +
                                department + "'. Allowed: " + String.join(", ", allowed));
                    }
                }

                // ── Email format validation ──────────────────────────────────────
                if (!personalEmail.isEmpty() && !isValidEmail(personalEmail)) {
                    rowErrors.add("Invalid Personal Email format: '" + personalEmail + "'");
                }
                if (!workEmail.isEmpty() && !isValidEmail(workEmail)) {
                    rowErrors.add("Invalid Work Email format: '" + workEmail + "'");
                }

                // ── Duplicate checks ─────────────────────────────────────────────
                if (!personalEmail.isEmpty() && rowErrors.stream().noneMatch(e -> e.contains("Personal Email"))) {
                    String norm = personalEmail.toLowerCase();
                    if (existingPersonalEmails.contains(norm)) {
                        rowErrors.add("Personal Email '" + personalEmail + "' already exists in the system");
                    } else if (batchPersonalEmails.contains(norm)) {
                        rowErrors.add("Personal Email '" + personalEmail + "' is duplicated within this upload file");
                    }
                }

                if (!workEmail.isEmpty() && rowErrors.stream().noneMatch(e -> e.contains("Work Email"))) {
                    String norm = workEmail.toLowerCase();
                    if (existingWorkEmails.contains(norm)) {
                        rowErrors.add("Work Email '" + workEmail + "' already exists in the system");
                    } else if (batchWorkEmails.contains(norm)) {
                        rowErrors.add("Work Email '" + workEmail + "' is duplicated within this upload file");
                    }
                }

                // ── Skip with reason ─────────────────────────────────────────────
                if (!rowErrors.isEmpty()) {
                    String reason = "Row " + excelRowNum + ": " + String.join("; ", rowErrors);
                    skippedReasons.add(reason);
                    skippedCount++;
                    logger.warn("[BulkUpload] Skipped {}", reason);
                    continue;
                }

                // ── Mark seen ────────────────────────────────────────────────────
                batchPersonalEmails.add(personalEmail.toLowerCase());
                if (!workEmail.isEmpty()) batchWorkEmails.add(workEmail.toLowerCase());

                // ── Generate unique employeeId (same format as createEmployee) ───
                // Loop until we find a counter value not already taken in DB or this batch.
                // Handles gaps in existing IDs (e.g. EMP0003 deleted → skip it).
                String generatedEmployeeId;
                do {
                    generatedEmployeeId = String.format("EMPKMS%02d", employeeIdCounter++);
                } while (existingEmployeeIds.contains(generatedEmployeeId));
                existingEmployeeIds.add(generatedEmployeeId); // reserve within this batch

                // ── Build entity ─────────────────────────────────────────────────
                validEntities.add(buildEntity(generatedEmployeeId, firstName, middleName, lastName, gender,
                        department, designation, employmentType, basicSalary,
                        personalEmail, workEmail, mobile, alternate, bankName, accountNumber, ifscCode));

                // ── Flush batch ──────────────────────────────────────────────────
                if (validEntities.size() >= BATCH_SIZE) {
                    List<EmployeeEntity> saved = employeeRepository.saveAll(validEntities);
                    uploadedCount += saved.size();
                    saved.forEach(e -> {
                        if (e.getPersonalEmail() != null) existingPersonalEmails.add(e.getPersonalEmail().toLowerCase());
                        if (e.getWorkEmail() != null) existingWorkEmails.add(e.getWorkEmail().toLowerCase());
                    });
                    validEntities.clear();
                    logger.info("[BulkUpload] Flushed batch. Running total uploaded: {}", uploadedCount);
                }
            }

            // Save tail batch
            if (!validEntities.isEmpty()) {
                uploadedCount += employeeRepository.saveAll(validEntities).size();
                validEntities.clear();
            }

            response.setUploadedCount(uploadedCount);
            response.setSkippedCount(skippedCount);
            response.setSkippedReasons(skippedReasons);
            response.setMessage(buildMessage(uploadedCount, skippedCount));
            logger.info("[BulkUpload] Complete. Uploaded: {}, Skipped: {}", uploadedCount, skippedCount);

        } catch (Exception e) {
            logger.error("[BulkUpload] Fatal error during processing", e);
            response.setMessage("Upload failed due to a server error: " + e.getMessage() +
                    ". Ensure the file is a valid .xlsx and matches the provided template.");
            response.setUploadedCount(0);
        }

        return response;
    }

    @Override
    public List<BirthdayDTO> getTodaysBirthdays() {
        LocalDate today = LocalDate.now();
        return employeeRepository.findTodaysBirthdays().stream()
                .map(e -> toBirthdayDTO(e, today))
                .collect(Collectors.toList());
    }

    @Override
    public List<BirthdayDTO> getBirthdaysForYear(int year) {
        List<BirthdayDTO> result = employeeRepository.findAllActiveWithDateOfBirth().stream()
                .map(e -> {
                    LocalDate dob = e.getDateOfBirth();
                    LocalDate occursOn;
                    try {
                        occursOn = LocalDate.of(year, dob.getMonthValue(), dob.getDayOfMonth());
                    } catch (Exception ex) {
                        occursOn = LocalDate.of(year, 2, 28); // Feb 29 fallback on non-leap years
                    }
                    return toBirthdayDTO(e, occursOn);
                })
                .collect(Collectors.toList());
        result.sort(Comparator.comparing(BirthdayDTO::getOccursOn));
        return result;
    }

    private BirthdayDTO toBirthdayDTO(EmployeeEntity e, LocalDate occursOn) {
        int age = occursOn.getYear() - e.getDateOfBirth().getYear();
        String photoUrl = (e.getProfilePhotoImage() != null && e.getProfilePhotoImage().length > 0)
                ? generateImageUrl(e.getEmployeeId(), "profile") : null;

        BirthdayDTO dto = new BirthdayDTO(
                e.getEmployeeId(),
                e.getFullName() != null ? e.getFullName() : (e.getFirstName() + " " + e.getLastName()),
                e.getDepartment(), e.getDesignation(), photoUrl, e.getDateOfBirth(), age);
        dto.setOccursOn(occursOn);
        return dto;
    }

    // ─── Header Detection ─────────────────────────────────────────────────────

    /**
     * Scans the first MAX_SCAN_ROWS rows for one that contains "first name" (case-insensitive).
     * Returns the 0-based row index of the header row, or -1 if not found.
     */
    private static final int MAX_SCAN_ROWS = 5;

    private int findHeaderRow(Sheet sheet, DataFormatter formatter) {
        for (int i = 0; i <= Math.min(MAX_SCAN_ROWS, sheet.getLastRowNum()); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            for (Cell cell : row) {
                String val = formatter.formatCellValue(cell).trim().toLowerCase()
                        .replaceAll("[^a-z ]", ""); // strip *, #, etc.
                if (val.equals("first name") || val.equals("firstname")) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Builds a map of normalised-header-name → column-index from the detected header row.
     * Normalisation: lowercase + strip non-alpha-space chars (handles "First Name*" → "first name").
     */
    private Map<String, Integer> buildColumnMap(Row headerRow, DataFormatter formatter) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null) return map;
        for (Cell cell : headerRow) {
            String raw = formatter.formatCellValue(cell).trim();
            String normalised = raw.toLowerCase().replaceAll("[^a-z ]", "").trim();
            if (!normalised.isEmpty()) {
                map.put(normalised, cell.getColumnIndex());
            }
        }
        return map;
    }

    private List<String> validateRequiredColumns(Map<String, Integer> colMap) {
        List<String> missing = new ArrayList<>();
        for (String required : new String[]{"first name", "last name", "personal email", "department", "designation"}) {
            if (!colMap.containsKey(required)) missing.add(required);
        }
        return missing;
    }

    // ─── Row Helpers ──────────────────────────────────────────────────────────

    /**
     * Checks only the columns we actually care about (from colMap) for emptiness.
     * This avoids false-positives from styled-but-empty cells outside our columns
     * (which POI iterates because they have background fills from the template).
     */
    private boolean isRowBlank(Row row, DataFormatter formatter, Map<String, Integer> colMap) {
        for (int colIdx : colMap.values()) {
            Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null) {
                String val = formatter.formatCellValue(cell).trim();
                if (!val.isEmpty()) return false;
            }
        }
        return true;
    }

    private String cell(Row row, Map<String, Integer> colMap, String headerKey, DataFormatter formatter) {
        Integer colIdx = colMap.get(headerKey);
        if (colIdx == null) return "";
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return "";
        return formatter.formatCellValue(cell).trim();
    }

    private Double parseDouble(Row row, Map<String, Integer> colMap, String headerKey, DataFormatter formatter) {
        Integer colIdx = colMap.get(headerKey);
        if (colIdx == null) return null;
        Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
            String raw = formatter.formatCellValue(cell).trim().replaceAll(",", "");
            return raw.isEmpty() ? null : Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // ─── Entity Builder ───────────────────────────────────────────────────────

    private EmployeeEntity buildEntity(
            String employeeId,
            String firstName, String middleName, String lastName, String gender,
            String department, String designation, String employmentType,
            Double basicSalary, String personalEmail, String workEmail,
            String mobile, String alternate, String bankName,
            String accountNumber, String ifscCode) {

        EmployeeEntity e = new EmployeeEntity();
        e.setEmployeeId(employeeId);
        e.setFirstName(firstName);
        e.setMiddleName(nullIfEmpty(middleName));
        e.setLastName(lastName);
        e.setFullName(firstName + (middleName.isBlank() ? "" : " " + middleName) + " " + lastName);
        e.setGender(nullIfEmpty(gender));
        e.setDepartment(department.toUpperCase());
        e.setDesignation(designation);
        e.setEmploymentType(nullIfEmpty(employmentType));
        e.setBasicSalary(basicSalary);
        e.setPersonalEmail(personalEmail.toLowerCase());
        e.setWorkEmail(workEmail.isBlank() ? null : workEmail.toLowerCase());
        e.setMobileNumber(nullIfEmpty(mobile));
        e.setAlternateNumber(nullIfEmpty(alternate));
        e.setBankName(nullIfEmpty(bankName));
        e.setAccountNumber(nullIfEmpty(accountNumber));
        e.setIfscCode(nullIfEmpty(ifscCode) != null ? ifscCode.toUpperCase() : null);
        e.setStatus("ACTIVE");
        e.setProfileStatus("PENDING");
        e.setCreatedAt(LocalDate.now());
        e.setUpdatedAt(LocalDate.now());
        return e;
    }

    // ─── Utility ──────────────────────────────────────────────────────────────

    private boolean isValidEmail(String email) {
        return email.matches("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");
    }

    private String nullIfEmpty(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String buildMessage(int uploaded, int skipped) {
        if (uploaded == 0 && skipped == 0)
            return "No valid data rows found in the uploaded file. Ensure data starts below the header row.";
        if (skipped == 0)
            return "All " + uploaded + " employee(s) uploaded successfully.";
        if (uploaded == 0)
            return "Upload failed. All " + skipped + " row(s) were skipped due to validation errors. Check 'skippedReasons' for details.";
        return uploaded + " employee(s) uploaded successfully. " + skipped + " row(s) skipped. Check 'skippedReasons' for details.";
    }
}