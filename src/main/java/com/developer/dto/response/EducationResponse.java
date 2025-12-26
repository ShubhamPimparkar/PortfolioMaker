package com.developer.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.developer.entity.Education;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EducationResponse {

    private UUID id;
    private UUID userId;
    private String institution;
    private String degree;
    private String fieldOfStudy;
    private LocalDate startDate;
    private LocalDate endDate;
    private String grade;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static EducationResponse fromEntity(Education education) {
        EducationResponse response = new EducationResponse();
        response.setId(education.getId());
        response.setUserId(education.getUser().getId());
        response.setInstitution(education.getInstitution());
        response.setDegree(education.getDegree());
        response.setFieldOfStudy(education.getFieldOfStudy());
        response.setStartDate(education.getStartDate());
        response.setEndDate(education.getEndDate());
        response.setGrade(education.getGrade());
        response.setDescription(education.getDescription());
        response.setCreatedAt(education.getCreatedAt());
        response.setUpdatedAt(education.getUpdatedAt());
        return response;
    }
}

