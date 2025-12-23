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

    private Header header;
    private String professionalSummary;
    private List<String> skills;
    private List<ProjectItem> projects;
}


