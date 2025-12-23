package com.developer.dto.request;

import java.util.List;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectRequest {

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull
    @Size(min = 1)
    private List<@Size(max = 100) String> techStack;

    @URL
    @Size(max = 512)
    private String projectUrl;

    @URL
    @Size(max = 512)
    private String githubRepoUrl;

    @Size(max = 255)
    private String role;

    private Boolean isPublic;
}

