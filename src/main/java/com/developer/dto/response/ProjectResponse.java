package com.developer.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.developer.entity.Project;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProjectResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private List<String> techStack;
    private String projectUrl;
    private String githubRepoUrl;
    private String role;
    private Boolean isPublic;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProjectResponse fromEntity(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setUserId(project.getUser().getId());
        response.setTitle(project.getTitle());
        response.setDescription(project.getDescription());
        response.setTechStack(project.getTechStack());
        response.setProjectUrl(project.getProjectUrl());
        response.setGithubRepoUrl(project.getGithubRepoUrl());
        response.setRole(project.getRole());
        response.setIsPublic(project.getIsPublic());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        return response;
    }
}

