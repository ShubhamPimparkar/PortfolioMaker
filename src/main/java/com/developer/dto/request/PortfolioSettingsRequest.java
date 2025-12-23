package com.developer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioSettingsRequest {

    @NotBlank(message = "Template key is required")
    private String templateKey;

    @Pattern(regexp = "^$|^#[0-9A-Fa-f]{6}$", message = "Primary color must be a valid hex color (e.g., #FF5733) or empty")
    private String primaryColor;

    private String fontFamily;

    private Boolean showSkills;

    private Boolean showProjects;
}

