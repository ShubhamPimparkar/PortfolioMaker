package com.developer.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EducationRequest {

    @NotBlank
    @Size(max = 255)
    private String institution;

    @NotBlank
    @Size(max = 255)
    private String degree;

    @Size(max = 255)
    private String fieldOfStudy;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 100)
    private String grade;

    @Size(max = 5000)
    private String description;
}

