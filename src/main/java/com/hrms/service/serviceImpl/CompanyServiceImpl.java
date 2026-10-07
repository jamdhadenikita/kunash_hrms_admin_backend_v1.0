package com.hrms.service.serviceImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hrms.dto.request.CompanyRequestDTO;
import com.hrms.dto.response.CompanyResponseDTO;
import com.hrms.entity.CompanyEntity;
import com.hrms.repository.CompanyRepository;
import com.hrms.service.CompanyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;
    private final ObjectMapper      objectMapper;

    public CompanyServiceImpl(CompanyRepository companyRepository, ObjectMapper objectMapper) {
        this.companyRepository = companyRepository;
        this.objectMapper      = objectMapper;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE  (INSERT or UPDATE)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CompanyResponseDTO saveCompany(CompanyRequestDTO request) throws JsonProcessingException {

        CompanyEntity company;

        if (request.getId() != null && request.getId() > 0) {
            // UPDATE — load existing entity so logo is preserved
            company = companyRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Company not found with id: " + request.getId()));
        } else {
            // INSERT
            company = new CompanyEntity();
        }

        // Basic scalar fields
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

        // JSON fields — always overwrite (null or empty array should also clear the field)
        setJsonField(company, "workingDays",     request.getWorkingDays());
        setJsonField(company, "breakTimings",    request.getBreakTimings());
        setJsonField(company, "saturdayPolicy",  request.getSaturdayPolicy());
        setJsonField(company, "holidays",        request.getHolidays());
        setJsonField(company, "departments",     request.getDepartments());
        setJsonField(company, "designations",    request.getDesignations());
        setJsonField(company, "socialMedia",     request.getSocialMedia());
        setJsonField(company, "contactPersons",  request.getContactPersons());
        setJsonField(company, "bankDetails",     request.getBankDetails());

        CompanyEntity saved = companyRepository.save(company);
        return convertToResponse(saved);
    }


    private void setJsonField(CompanyEntity e, String fieldName, String json) {
        // Always set — even if empty — so a user can clear all departments / designations
        switch (fieldName) {
            case "workingDays"     -> e.setWorkingDaysJson(json);
            case "breakTimings"    -> e.setBreakTimingsJson(json);
            case "saturdayPolicy"  -> e.setSaturdayPolicyJson(json);
            case "holidays"        -> e.setHolidaysJson(json);
            case "departments"     -> e.setDepartmentsJson(json);
            case "designations"    -> e.setDesignationsJson(json);
            case "socialMedia"     -> e.setSocialMediaJson(json);
            case "contactPersons"  -> e.setContactPersonsJson(json);
            case "bankDetails"     -> e.setBankDetailsJson(json);
        }
    }
    // ─────────────────────────────────────────────────────────────────────────
    // READ
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public CompanyResponseDTO getCompany(Long id) {
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        return convertToResponse(company);
    }

    @Override
    public CompanyResponseDTO getCompanyByEmail(String email) {
        CompanyEntity company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Company not found with email: " + email));
        return convertToResponse(company);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteCompany(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FILTER  (by department / designation — used for employee assignment etc.)
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<CompanyResponseDTO> getCompaniesByDepartment(String department) {
        return companyRepository.findByDepartment(department)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    public List<CompanyResponseDTO> getCompaniesByDesignation(String designation) {
        return companyRepository.findByDesignation(designation)
                .stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL DEPARTMENTS   →  GET /api/company/departments
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<String> getAllDepartments() {
        List<String> allJson = companyRepository.findAllDepartmentsJson();
        Set<String> result   = new LinkedHashSet<>();

        for (String json : allJson) {
            if (json == null || json.isBlank()) continue;
            try {
                List<Map<String, Object>> list = parseJsonArray(json);
                for (Map<String, Object> item : list) {
                    // "departmentName" is the canonical key used by this frontend
                    String name = extractString(item, "departmentName", "name", "title");
                    if (name != null) result.add(name.trim());
                }
            } catch (Exception e) {
                handlePlainStringArray(json, result);
            }
        }

        return result.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL DESIGNATIONS  →  GET /api/company/designations
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<String> getAllDesignations() {
        List<String> allJson = companyRepository.findAllDesignationsJson();
        Set<String> result   = new LinkedHashSet<>();

        for (String json : allJson) {
            if (json == null || json.isBlank()) continue;
            try {
                List<Map<String, Object>> list = parseJsonArray(json);
                for (Map<String, Object> item : list) {
                    // "designationName" is the canonical key used by this frontend
                    String name = extractString(item, "designationName", "name", "title");
                    if (name != null) result.add(name.trim());
                }
            } catch (Exception e) {
                handlePlainStringArray(json, result);
            }
        }

        return result.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL DESIGNATION OBJECTS  →  GET /api/company/designation-details
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<Map<String, Object>> getAllDesignationObjects() {
        List<String> allJson   = companyRepository.findAllDesignationsJson();
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> seen       = new LinkedHashSet<>();

        for (String json : allJson) {
            if (json == null || json.isBlank()) continue;
            try {
                List<Map<String, Object>> list = parseJsonArray(json);
                for (Map<String, Object> item : list) {
                    String name = extractString(item, "designationName", "name");
                    if (name != null && seen.add(name.trim())) {
                        result.add(item);
                    }
                }
            } catch (Exception ignored) { }
        }

        result.sort(Comparator.comparing(
                m -> String.valueOf(m.getOrDefault("designationName", "")),
                String.CASE_INSENSITIVE_ORDER
        ));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL DEPARTMENT OBJECTS  →  GET /api/company/department-details
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public List<Map<String, Object>> getAllDepartmentObjects() {
        List<String> allJson   = companyRepository.findAllDepartmentsJson();
        List<Map<String, Object>> result = new ArrayList<>();
        Set<String> seen       = new LinkedHashSet<>();

        for (String json : allJson) {
            if (json == null || json.isBlank()) continue;
            try {
                List<Map<String, Object>> list = parseJsonArray(json);
                for (Map<String, Object> item : list) {
                    String name = extractString(item, "departmentName", "name");
                    if (name != null && seen.add(name.trim())) {
                        result.add(item);
                    }
                }
            } catch (Exception ignored) { }
        }

        result.sort(Comparator.comparing(
                m -> String.valueOf(m.getOrDefault("departmentName", "")),
                String.CASE_INSENSITIVE_ORDER
        ));
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Parse a JSON string into a List of Maps. */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseJsonArray(String json) throws Exception {
        return objectMapper.readValue(json,
                objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class));
    }


    private String extractString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }

    private void handlePlainStringArray(String json, Set<String> target) {
        try {
            List<String> plain = objectMapper.readValue(json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            plain.stream().filter(s -> s != null && !s.isBlank()).map(String::trim).forEach(target::add);
        } catch (Exception ex) {
            System.err.println("[CompanyService] Could not parse JSON: " + json);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ENTITY → RESPONSE DTO
    // ─────────────────────────────────────────────────────────────────────────

    private CompanyResponseDTO convertToResponse(CompanyEntity c) {
        CompanyResponseDTO dto = new CompanyResponseDTO();
        dto.setId(c.getId());
        dto.setCompanyName(c.getCompanyName());
        dto.setGstNumber(c.getGstNumber());
        dto.setPanNumber(c.getPanNumber());
        dto.setCinNumber(c.getCinNumber());
        dto.setRegistrationNumber(c.getRegistrationNumber());
        dto.setEmail(c.getEmail());
        dto.setPhone(c.getPhone());
        dto.setWebsite(c.getWebsite());
        dto.setEstablishedYear(c.getEstablishedYear());
        dto.setEmployeeCount(c.getEmployeeCount());
        dto.setCompanyType(c.getCompanyType());
        dto.setIndustryType(c.getIndustryType());
        dto.setAddress(c.getAddress());
        dto.setDescription(c.getDescription());

        if (c.getLogo() != null && c.getLogo().length > 0) {
            dto.setLogoUrl("/api/company/logo/" + c.getId());
            dto.setLogoContentType(c.getLogoContentType());
        }

        dto.setWorkingDaysJson(c.getWorkingDaysJson());
        dto.setBreakTimingsJson(c.getBreakTimingsJson());
        dto.setSaturdayPolicyJson(c.getSaturdayPolicyJson());
        dto.setHolidaysJson(c.getHolidaysJson());
        dto.setDepartmentsJson(c.getDepartmentsJson());
        dto.setDesignationsJson(c.getDesignationsJson());
        dto.setSocialMediaJson(c.getSocialMediaJson());
        dto.setContactPersonsJson(c.getContactPersonsJson());
        dto.setBankDetailsJson(c.getBankDetailsJson());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        return dto;
    }
}