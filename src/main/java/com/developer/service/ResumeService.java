package com.developer.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.response.ResumeDTO;
import com.developer.dto.response.ResumeDTO.AchievementItem;
import com.developer.dto.response.ResumeDTO.EducationItem;
import com.developer.dto.response.ResumeDTO.Header;
import com.developer.dto.response.ResumeDTO.ProjectItem;
import com.developer.entity.Achievement;
import com.developer.entity.Education;
import com.developer.entity.Profile;
import com.developer.entity.Project;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.ResumeGenerationException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.AchievementRepository;
import com.developer.repository.EducationRepository;
import com.developer.repository.ProfileRepository;
import com.developer.repository.ProjectRepository;
import com.developer.repository.UserRepository;

@Service
public class ResumeService {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final EducationRepository educationRepository;
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    public ResumeService(ProfileRepository profileRepository,
                         ProjectRepository projectRepository,
                         EducationRepository educationRepository,
                         AchievementRepository achievementRepository,
                         UserRepository userRepository) {
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.educationRepository = educationRepository;
        this.achievementRepository = achievementRepository;
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

    @Transactional(readOnly = true)
    public ResumeDTO buildResumeForCurrentUser() {
        User user = getCurrentUser();
        try {
            Profile profile = profileRepository.findByUser(user)
                    .orElseThrow(() -> new ResumeGenerationException("Profile not found for current user"));

            // Fully materialize projects list and filter public ones inside the transaction
            List<Project> projects = projectRepository.findByUserOrderByCreatedAtDesc(user)
                    .stream()
                    .filter(p -> Boolean.TRUE.equals(p.getIsPublic()))
                    .collect(Collectors.toList());

            // Fetch education and achievements
            List<Education> educationList = educationRepository.findByUserOrderByStartDateDesc(user);
            List<Achievement> achievementList = achievementRepository.findByUserOrderByIssueDateDescCreatedAtDesc(user);

            Header header = Header.builder()
                    .fullName(profile.getFullName())
                    .headline(profile.getHeadline())
                    .email(user.getEmail())
                    .location(profile.getLocation())
                    .githubUrl(profile.getGithubUrl())
                    .linkedinUrl(profile.getLinkedinUrl())
                    .portfolioUrl(profile.getPortfolioUrl())
                    .yearsOfExperience(profile.getYearsOfExperience())
                    .build();

            // Copy out collections so DTO contains plain Java lists, not lazy Hibernate collections
            List<String> skills = profile.getSkills() != null
                    ? List.copyOf(profile.getSkills())
                    : List.of();

            List<ProjectItem> projectItems = projects.stream()
                    .map(p -> ProjectItem.builder()
                            .title(p.getTitle())
                            .role(p.getRole())
                            .description(p.getDescription())
                            .techStack(p.getTechStack() != null ? List.copyOf(p.getTechStack()) : List.of())
                            .projectUrl(p.getProjectUrl())
                            .githubRepoUrl(p.getGithubRepoUrl())
                            .build())
                    .collect(Collectors.toList());

            List<EducationItem> educationItems = educationList.stream()
                    .map(e -> EducationItem.builder()
                            .institution(e.getInstitution())
                            .degree(e.getDegree())
                            .fieldOfStudy(e.getFieldOfStudy())
                            .startDate(e.getStartDate())
                            .endDate(e.getEndDate())
                            .grade(e.getGrade())
                            .description(e.getDescription())
                            .build())
                    .collect(Collectors.toList());

            List<AchievementItem> achievementItems = achievementList.stream()
                    .map(a -> AchievementItem.builder()
                            .title(a.getTitle())
                            .issuer(a.getIssuer())
                            .issueDate(a.getIssueDate())
                            .description(a.getDescription())
                            .link(a.getLink())
                            .build())
                    .collect(Collectors.toList());

            return ResumeDTO.builder()
                    .header(header)
                    .professionalSummary(profile.getSummary())
                    .skills(skills)
                    .projects(projectItems)
                    .education(educationItems)
                    .achievements(achievementItems)
                    .build();
        }
        catch(Exception e){
            throw new ResumeGenerationException("Failed to generate resume PDF", e);
        }
    }
}


