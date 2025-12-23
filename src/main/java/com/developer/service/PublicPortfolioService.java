package com.developer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.response.PublicPortfolioResponse;
import com.developer.entity.PortfolioSettings;
import com.developer.entity.Profile;
import com.developer.entity.Project;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.repository.ProfileRepository;
import com.developer.repository.ProjectRepository;
import com.developer.repository.UserRepository;

@Service
public class PublicPortfolioService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioSettingsService portfolioSettingsService;

    public PublicPortfolioService(UserRepository userRepository,
                                  ProfileRepository profileRepository,
                                  ProjectRepository projectRepository,
                                  PortfolioSettingsService portfolioSettingsService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.portfolioSettingsService = portfolioSettingsService;
    }

    @Transactional(readOnly = true)
    public PublicPortfolioResponse getPublicPortfolioByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<Project> publicProjects = projectRepository.findByUserAndIsPublicTrueOrderByCreatedAtDesc(user);
        PortfolioSettings settings = portfolioSettingsService.getSettingsByUser(user);

        PublicPortfolioResponse response = new PublicPortfolioResponse();

        // Map profile data
        PublicPortfolioResponse.PublicProfileDto profileDto = new PublicPortfolioResponse.PublicProfileDto();
        profileDto.setFullName(profile.getFullName());
        profileDto.setHeadline(profile.getHeadline());
        profileDto.setSummary(profile.getSummary());
        profileDto.setLocation(profile.getLocation());
        profileDto.setYearsOfExperience(profile.getYearsOfExperience());
        // Respect showSkills visibility flag
        if (settings.getShowSkills() != null && settings.getShowSkills()) {
            profileDto.setSkills(profile.getSkills());
        } else {
            profileDto.setSkills(List.of());
        }
        profileDto.setGithubUrl(profile.getGithubUrl());
        profileDto.setLinkedinUrl(profile.getLinkedinUrl());
        profileDto.setPortfolioUrl(profile.getPortfolioUrl());
        response.setProfile(profileDto);

        // Map projects (only public ones, respecting visibility flag)
        if (settings.getShowProjects() != null && settings.getShowProjects()) {
            List<PublicPortfolioResponse.PublicProjectDto> projectDtos = publicProjects.stream()
                    .map(project -> {
                        PublicPortfolioResponse.PublicProjectDto dto = new PublicPortfolioResponse.PublicProjectDto();
                        dto.setTitle(project.getTitle());
                        dto.setDescription(project.getDescription());
                        dto.setTechStack(project.getTechStack());
                        dto.setProjectUrl(project.getProjectUrl());
                        dto.setGithubRepoUrl(project.getGithubRepoUrl());
                        dto.setRole(project.getRole());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setProjects(projectDtos);
        } else {
            response.setProjects(List.of());
        }

        // Map settings
        PublicPortfolioResponse.PublicSettingsDto settingsDto = new PublicPortfolioResponse.PublicSettingsDto();
        settingsDto.setTemplateKey(settings.getTemplateKey());
        settingsDto.setPrimaryColor(settings.getPrimaryColor());
        settingsDto.setFontFamily(settings.getFontFamily());
        settingsDto.setShowSkills(settings.getShowSkills());
        settingsDto.setShowProjects(settings.getShowProjects());
        response.setSettings(settingsDto);

        return response;
    }
}
