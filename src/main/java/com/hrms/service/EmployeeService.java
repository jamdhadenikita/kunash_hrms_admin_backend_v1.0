package com.hrms.service;

import com.hrms.dto.request.DashboardDTO;
import com.hrms.dto.request.EmployeeRequestDTO;
import com.hrms.dto.request.RegisterEmployeeRequestDTO;
import com.hrms.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EmployeeService {


    List<EmployeeSummaryDTO> getAllEmployees();

    // Create
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO);

    // Update
    EmployeeResponseDTO updateEmployee(Long employeePrimeId, EmployeeRequestDTO requestDTO);

    // Get single employee
    EmployeeResponseDTO getEmployeeById(Long employeePrimeId);
    EmployeeResponseDTO getEmployeeByEmployeePrimeId(String employeePrimeId);  // ✅ Changed
    EmployeeResponseDTO getEmployeeByEmail(String email);

    // Get multiple employees (paginated)
    Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable);
    Page<EmployeeResponseDTO> getEmployeesByDepartment(String department, Pageable pageable);
    Page<EmployeeResponseDTO> getEmployeesByStatus(String status, Pageable pageable);
//    Page<EmployeeResponseDTO> searchEmployees(String search, Pageable pageable);

    // Get all employees without pagination (for export)
    List<EmployeeResponseDTO> getAllEmployeesList();

    // Delete
    void deleteEmployee(Long employeePrimeId);
    void bulkDeleteEmployees(List<Long> ids);

    // Update status
    EmployeeResponseDTO updateEmployeeStatus(Long employeePrimeId, String status);

    // Count
    long getTotalCount();

    List<EmployeeForPayrollDTO> getEmployeesForPayroll(String department);


    EmployeeLoginResponseDto login(String employeeId, String password);

    void updatePassword(String employeeId, String oldPassword, String newPassword);

    public RegisteredEmployeeResponseDTO registerEmployee(RegisterEmployeeRequestDTO dto);

    DashboardDTO getDashboardData(Long employeePrimeId);

    /**
     * Parses the uploaded Excel file, validates each row,
     * skips duplicates / invalid rows with reasons, and batch-saves valid employees.
     *
     * @param file the uploaded .xlsx file
     * @return BulkEmployeeResponse with upload/skip counts and skip reasons
     */
    BulkEmployeeResponse processBulkUpload(MultipartFile file);

  ///////////////////////////////
    List<BirthdayDTO> getTodaysBirthdays();
    
    List<BirthdayDTO> getBirthdaysForYear(int year);

}
