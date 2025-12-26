package com.developer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.response.PublicPortfolioResponse;
import com.developer.entity.Achievement;
import com.developer.entity.Education;
import com.developer.entity.PortfolioSettings;
import com.developer.entity.Profile;
import com.developer.entity.Project;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.repository.AchievementRepository;
import com.developer.repository.EducationRepository;
import com.developer.repository.ProfileRepository;
import com.developer.repository.ProjectRepository;
import com.developer.repository.UserRepository;

@Service
public class PublicPortfolioService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final EducationRepository educationRepository;
    private final AchievementRepository achievementRepository;
    private final PortfolioSettingsService portfolioSettingsService;

    public PublicPortfolioService(UserRepository userRepository,
                                  ProfileRepository profileRepository,
                                  ProjectRepository projectRepository,
                                  EducationRepository educationRepository,
                                  AchievementRepository achievementRepository,
                                  PortfolioSettingsService portfolioSettingsService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.educationRepository = educationRepository;
        this.achievementRepository = achievementRepository;
        this.portfolioSettingsService = portfolioSettingsService;
    }

    @Transactional(readOnly = true)
    public PublicPortfolioResponse getPublicPortfolioByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<Project> publicProjects = projectRepository.findByUserAndIsPublicTrueOrderByCreatedAtDesc(user);
        List<Education> educationList = educationRepository.findByUserOrderByStartDateDesc(user);
        List<Achievement> achievementList = achievementRepository.findByUserOrderByIssueDateDescCreatedAtDesc(user);
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

        // Map education (respecting visibility flag)
        if (settings.getShowEducation() != null && settings.getShowEducation()) {
            List<PublicPortfolioResponse.PublicEducationDto> educationDtos = educationList.stream()
                    .map(education -> {
                        PublicPortfolioResponse.PublicEducationDto dto = new PublicPortfolioResponse.PublicEducationDto();
                        dto.setInstitution(education.getInstitution());
                        dto.setDegree(education.getDegree());
                        dto.setFieldOfStudy(education.getFieldOfStudy());
                        dto.setStartDate(education.getStartDate());
                        dto.setEndDate(education.getEndDate());
                        dto.setGrade(education.getGrade());
                        dto.setDescription(education.getDescription());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setEducation(educationDtos);
        } else {
            response.setEducation(List.of());
        }

        // Map achievements (respecting visibility flag)
        if (settings.getShowAchievements() != null && settings.getShowAchievements()) {
            List<PublicPortfolioResponse.PublicAchievementDto> achievementDtos = achievementList.stream()
                    .map(achievement -> {
                        PublicPortfolioResponse.PublicAchievementDto dto = new PublicPortfolioResponse.PublicAchievementDto();
                        dto.setTitle(achievement.getTitle());
                        dto.setIssuer(achievement.getIssuer());
                        dto.setIssueDate(achievement.getIssueDate());
                        dto.setDescription(achievement.getDescription());
                        dto.setLink(achievement.getLink());
                        return dto;
                    })
                    .collect(Collectors.toList());
            response.setAchievements(achievementDtos);
        } else {
            response.setAchievements(List.of());
        }

        // Map settings
        PublicPortfolioResponse.PublicSettingsDto settingsDto = new PublicPortfolioResponse.PublicSettingsDto();
        settingsDto.setTemplateKey(settings.getTemplateKey());
        settingsDto.setPrimaryColor(settings.getPrimaryColor());
        settingsDto.setFontFamily(settings.getFontFamily());
        settingsDto.setShowSkills(settings.getShowSkills());
        settingsDto.setShowProjects(settings.getShowProjects());
        settingsDto.setShowEducation(settings.getShowEducation());
        settingsDto.setShowAchievements(settings.getShowAchievements());
        response.setSettings(settingsDto);

        return response;
    }
}
