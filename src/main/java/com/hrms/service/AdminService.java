package com.hrms.service;

import com.hrms.dto.request.AdminRequestDto;
import com.hrms.dto.response.AdminResponseDto;

import java.util.List;

public interface AdminService {

    AdminResponseDto createAdmin(AdminRequestDto requestDto);

    AdminResponseDto getAdminByAdminId(String adminId);

    List<AdminResponseDto> getAllAdmins();

    AdminResponseDto updateAdmin(String adminId, AdminRequestDto requestDto);

    AdminResponseDto patchAdmin(String adminId, AdminRequestDto requestDto);

    AdminResponseDto updateModuleAccess(String adminId, List<String> moduleAccess);   // ← NEW

    void deleteAdmin(String adminId);
}