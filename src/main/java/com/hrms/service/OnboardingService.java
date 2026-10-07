package com.hrms.service;


import com.hrms.dto.request.OnboardingRequestDTO;
import com.hrms.dto.response.OnboardingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OnboardingService {

    OnboardingResponseDTO createOnboarding(OnboardingRequestDTO dto);

    OnboardingResponseDTO updateOnboarding(Long id, OnboardingRequestDTO dto);

    OnboardingResponseDTO getOnboardingById(Long id);

    OnboardingResponseDTO getOnboardingByEmployeePrimeId(String employeePrimeId);

    Page<OnboardingResponseDTO> getAllOnboarding(Pageable pageable);

    Page<OnboardingResponseDTO> getOnboardingByStatus(String status, Pageable pageable);

    void deleteOnboarding(Long id);

    OnboardingResponseDTO updateStatus(Long id, String status);

    long getTotalCount();

    void cleanupStaleOnboardingRecords();
}