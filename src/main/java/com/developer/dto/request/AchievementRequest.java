package com.developer.dto.request;

import java.time.LocalDate;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AchievementRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 255)
    private String issuer;

    private LocalDate issueDate;

    @Size(max = 5000)
    private String description;

    @URL
    @Size(max = 512)
    private String link;
}

