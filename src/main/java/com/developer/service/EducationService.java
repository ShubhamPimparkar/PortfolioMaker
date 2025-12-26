package com.developer.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.request.EducationRequest;
import com.developer.dto.response.EducationResponse;
import com.developer.entity.Education;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.EducationRepository;
import com.developer.repository.UserRepository;

@Service
public class EducationService {

    private final EducationRepository educationRepository;
    private final UserRepository userRepository;

    public EducationService(EducationRepository educationRepository, UserRepository userRepository) {
        this.educationRepository = educationRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public EducationResponse createEducation(EducationRequest request) {
        User user = getCurrentUser();

        validateEducationDates(request.getStartDate(), request.getEndDate());

        Education education = new Education();
        education.setUser(user);
        education.setInstitution(request.getInstitution());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setGrade(request.getGrade());
        education.setDescription(request.getDescription());

        Education saved = educationRepository.save(education);
        return EducationResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<EducationResponse> getCurrentUserEducation() {
        User user = getCurrentUser();
        return educationRepository.findByUserOrderByStartDateDesc(user)
                .stream()
                .map(EducationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public EducationResponse updateEducation(UUID educationId, EducationRequest request) {
        User user = getCurrentUser();
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));

        // Ownership check
        if (!education.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this education record");
        }

        validateEducationDates(request.getStartDate(), request.getEndDate());

        education.setInstitution(request.getInstitution());
        education.setDegree(request.getDegree());
        education.setFieldOfStudy(request.getFieldOfStudy());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setGrade(request.getGrade());
        education.setDescription(request.getDescription());

        Education saved = educationRepository.save(education);
        return EducationResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteEducation(UUID educationId) {
        User user = getCurrentUser();
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education not found"));

        // Ownership check
        if (!education.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this education record");
        }

        educationRepository.delete(education);
    }

    private void validateEducationDates(java.time.LocalDate startDate, java.time.LocalDate endDate) {
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }
}

