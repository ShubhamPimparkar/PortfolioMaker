package com.developer.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileRequest {

    @NotBlank
    @Size(max = 255)
    private String fullName;

    @NotBlank
    @Size(max = 255)
    private String headline;

    @Size(max = 2000)
    private String summary;

    @Size(max = 255)
    private String location;

    @Min(0)
    @Max(60)
    private Integer yearsOfExperience;

    @NotNull
    @Size(min = 1)
    private List<@Size(max = 100) String> skills;

    @URL
    @Size(max = 512)
    private String githubUrl;

    @URL
    @Size(max = 512)
    private String linkedinUrl;

    @URL
    @Size(max = 512)
    private String portfolioUrl;
}

