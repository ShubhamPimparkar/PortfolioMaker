package com.developer.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PublicPortfolioResponse {

    private PublicProfileDto profile;
    private List<PublicProjectDto> projects;
    private PublicSettingsDto settings;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PublicProfileDto {
        private String fullName;
        private String headline;
        private String summary;
        private String location;
        private Integer yearsOfExperience;
        private List<String> skills;
        private String githubUrl;
        private String linkedinUrl;
        private String portfolioUrl;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PublicProjectDto {
        private String title;
        private String description;
        private List<String> techStack;
        private String projectUrl;
        private String githubRepoUrl;
        private String role;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class PublicSettingsDto {
        private String templateKey;
        private String primaryColor;
        private String fontFamily;
        private Boolean showSkills;
        private Boolean showProjects;
    }
}

