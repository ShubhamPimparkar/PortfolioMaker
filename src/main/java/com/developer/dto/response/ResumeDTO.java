package com.developer.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResumeDTO {

    @Getter
    @Builder
    public static class Header {
        private String fullName;
        private String headline;
        private String email;
        private String location;
        private String githubUrl;
        private String linkedinUrl;
        private String portfolioUrl;
        private Integer yearsOfExperience;
    }

    @Getter
    @Builder
    public static class ProjectItem {
        private String title;
        private String role;
        private String description;
        private List<String> techStack;
        private String projectUrl;
        private String githubRepoUrl;
    }

    @Getter
    @Builder
    public static class EducationItem {
        private String institution;
        private String degree;
        private String fieldOfStudy;
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private String grade;
        private String description;
    }

    @Getter
    @Builder
    public static class AchievementItem {
        private String title;
        private String issuer;
        private java.time.LocalDate issueDate;
        private String description;
        private String link;
    }

    private Header header;
    private String professionalSummary;
    private List<String> skills;
    private List<ProjectItem> projects;
    private List<EducationItem> education;
    private List<AchievementItem> achievements;
}


