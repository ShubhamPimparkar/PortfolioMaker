package com.developer.service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.request.ProjectRequest;
import com.developer.dto.response.ProjectResponse;
import com.developer.entity.Project;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.ProjectRepository;
import com.developer.repository.UserRepository;

@Service
public class ProjectService {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?github\\.com/[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?/[a-zA-Z0-9._-]+/?$"
    );

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
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
    public ProjectResponse createProject(ProjectRequest request) {
        User user = getCurrentUser();

        validateProjectUrls(request.getProjectUrl(), request.getGithubRepoUrl());

        Project project = new Project();
        project.setUser(user);
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setProjectUrl(request.getProjectUrl());
        project.setGithubRepoUrl(request.getGithubRepoUrl());
        project.setRole(request.getRole());
        project.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : false);

        Project saved = projectRepository.save(project);
        return ProjectResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getCurrentUserProjects() {
        User user = getCurrentUser();
        return projectRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(ProjectResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, ProjectRequest request) {
        User user = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Ownership check
        if (!project.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this project");
        }

        validateProjectUrls(request.getProjectUrl(), request.getGithubRepoUrl());

        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setTechStack(request.getTechStack());
        project.setProjectUrl(request.getProjectUrl());
        project.setGithubRepoUrl(request.getGithubRepoUrl());
        project.setRole(request.getRole());
        project.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : project.getIsPublic());

        Project saved = projectRepository.save(project);
        return ProjectResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteProject(UUID projectId) {
        User user = getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Ownership check
        if (!project.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this project");
        }

        projectRepository.delete(project);
    }

    private void validateProjectUrls(String projectUrl, String githubRepoUrl) {
        if (githubRepoUrl != null && !githubRepoUrl.isBlank() && !GITHUB_URL_PATTERN.matcher(githubRepoUrl).matches()) {
            throw new IllegalArgumentException("Invalid GitHub repository URL format");
        }
        // Project URL can be any valid URL, so we just check if it's not empty when provided
        if (projectUrl != null && !projectUrl.isBlank()) {
            try {
                new java.net.URL(projectUrl);
            } catch (java.net.MalformedURLException e) {
                throw new IllegalArgumentException("Invalid project URL format");
            }
        }
    }
}

