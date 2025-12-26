package com.developer.dashboard.health;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.analytics.summary.PortfolioAnalyticsSummary;
import com.developer.analytics.summary.PortfolioAnalyticsSummaryRepository;
import com.developer.dashboard.health.dto.HealthCheck;
import com.developer.dashboard.health.dto.PortfolioHealthResponse;
import com.developer.entity.Achievement;
import com.developer.entity.Education;
import com.developer.entity.Profile;
import com.developer.entity.Project;
import com.developer.entity.User;
import com.developer.repository.AchievementRepository;
import com.developer.repository.EducationRepository;
import com.developer.repository.ProfileRepository;
import com.developer.repository.ProjectRepository;

/**
 * Service responsible for computing portfolio health score.
 * 
 * Scoring breakdown:
 * - PROFILE (30 points): Basic profile (15), Skills (10), Summary (5)
 * - PROJECTS (25 points): At least 1 project (10), At least 3 projects (15)
 * - EDUCATION & ACHIEVEMENTS (20 points): Education (10), Achievement (10)
 * - ANALYTICS (15 points): At least 1 view (5), Engagement rate ≥ 50% (10)
 * - ACTIVITY (10 points): Updated in last 30 days (10)
 * 
 * Total: 100 points
 */
@Service
public class PortfolioHealthService {

    // Scoring constants
    private static final int PROFILE_BASIC_POINTS = 15;
    private static final int PROFILE_SKILLS_POINTS = 10;
    private static final int PROFILE_SUMMARY_POINTS = 5;
    
    private static final int PROJECTS_ONE_POINTS = 10;
    private static final int PROJECTS_THREE_POINTS = 15;
    
    private static final int EDUCATION_POINTS = 10;
    private static final int ACHIEVEMENT_POINTS = 10;
    
    private static final int ANALYTICS_VIEW_POINTS = 5;
    private static final int ANALYTICS_ENGAGEMENT_POINTS = 10;
    private static final int ENGAGEMENT_THRESHOLD_PERCENT = 50;
    
    private static final int ACTIVITY_RECENT_POINTS = 10;
    private static final int ACTIVITY_DAYS_THRESHOLD = 30;

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final EducationRepository educationRepository;
    private final AchievementRepository achievementRepository;
    private final PortfolioAnalyticsSummaryRepository analyticsSummaryRepository;

    public PortfolioHealthService(
            ProfileRepository profileRepository,
            ProjectRepository projectRepository,
            EducationRepository educationRepository,
            AchievementRepository achievementRepository,
            PortfolioAnalyticsSummaryRepository analyticsSummaryRepository) {
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.educationRepository = educationRepository;
        this.achievementRepository = achievementRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
    }

    /**
     * Computes portfolio health score for a user.
     * 
     * @param user The user to compute health for
     * @return PortfolioHealthResponse with score and checks
     */
    @Transactional(readOnly = true)
    public PortfolioHealthResponse computeHealth(User user) {
        List<HealthCheck> checks = new ArrayList<>();
        int totalScore = 0;

        // Fetch all required data
        Optional<Profile> profileOpt = profileRepository.findByUser(user);
        List<Project> projects = projectRepository.findByUserOrderByCreatedAtDesc(user);
        List<Education> educationList = educationRepository.findByUserOrderByStartDateDesc(user);
        List<Achievement> achievementList = achievementRepository.findByUserOrderByIssueDateDescCreatedAtDesc(user);
        Optional<PortfolioAnalyticsSummary> analyticsOpt = analyticsSummaryRepository.findByPortfolioUserId(user.getId());

        // PROFILE SCORING (30 points)
        totalScore += scoreProfile(profileOpt, checks);

        // PROJECTS SCORING (25 points)
        totalScore += scoreProjects(projects, checks);

        // EDUCATION & ACHIEVEMENTS SCORING (20 points)
        totalScore += scoreEducationAndAchievements(educationList, achievementList, checks);

        // ANALYTICS SCORING (15 points)
        totalScore += scoreAnalytics(analyticsOpt, checks);

        // ACTIVITY SCORING (10 points)
        totalScore += scoreActivity(profileOpt, projects, checks);

        PortfolioHealthResponse response = new PortfolioHealthResponse();
        response.setScore(totalScore);
        response.setChecks(checks);

        return response;
    }

    private int scoreProfile(Optional<Profile> profileOpt, List<HealthCheck> checks) {
        int score = 0;

        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            
            // Basic profile (15 points)
            boolean hasBasicProfile = profile.getFullName() != null && !profile.getFullName().isBlank()
                    && profile.getHeadline() != null && !profile.getHeadline().isBlank();
            if (hasBasicProfile) {
                score += PROFILE_BASIC_POINTS;
                checks.add(new HealthCheck("Complete your profile", true, PROFILE_BASIC_POINTS));
            } else {
                checks.add(new HealthCheck("Complete your profile", false, PROFILE_BASIC_POINTS));
            }

            // Skills (10 points)
            boolean hasSkills = profile.getSkills() != null && !profile.getSkills().isEmpty();
            if (hasSkills) {
                score += PROFILE_SKILLS_POINTS;
                checks.add(new HealthCheck("Add skills to your profile", true, PROFILE_SKILLS_POINTS));
            } else {
                checks.add(new HealthCheck("Add skills to your profile", false, PROFILE_SKILLS_POINTS));
            }

            // Summary (5 points)
            boolean hasSummary = profile.getSummary() != null && !profile.getSummary().isBlank();
            if (hasSummary) {
                score += PROFILE_SUMMARY_POINTS;
                checks.add(new HealthCheck("Add a professional summary", true, PROFILE_SUMMARY_POINTS));
            } else {
                checks.add(new HealthCheck("Add a professional summary", false, PROFILE_SUMMARY_POINTS));
            }
        } else {
            checks.add(new HealthCheck("Complete your profile", false, PROFILE_BASIC_POINTS));
            checks.add(new HealthCheck("Add skills to your profile", false, PROFILE_SKILLS_POINTS));
            checks.add(new HealthCheck("Add a professional summary", false, PROFILE_SUMMARY_POINTS));
        }

        return score;
    }

    private int scoreProjects(List<Project> projects, List<HealthCheck> checks) {
        int score = 0;
        int projectCount = projects.size();

        // At least 1 project (10 points)
        if (projectCount >= 1) {
            score += PROJECTS_ONE_POINTS;
            checks.add(new HealthCheck("Add at least 1 project", true, PROJECTS_ONE_POINTS));
        } else {
            checks.add(new HealthCheck("Add at least 1 project", false, PROJECTS_ONE_POINTS));
        }

        // At least 3 projects (15 points)
        if (projectCount >= 3) {
            score += PROJECTS_THREE_POINTS;
            checks.add(new HealthCheck("Add at least 3 projects", true, PROJECTS_THREE_POINTS));
        } else {
            checks.add(new HealthCheck("Add at least 3 projects", false, PROJECTS_THREE_POINTS));
        }

        return score;
    }

    private int scoreEducationAndAchievements(List<Education> educationList, List<Achievement> achievementList, 
                                             List<HealthCheck> checks) {
        int score = 0;

        // Education (10 points)
        boolean hasEducation = !educationList.isEmpty();
        if (hasEducation) {
            score += EDUCATION_POINTS;
            checks.add(new HealthCheck("Add your education", true, EDUCATION_POINTS));
        } else {
            checks.add(new HealthCheck("Add your education", false, EDUCATION_POINTS));
        }

        // Achievement (10 points)
        boolean hasAchievement = !achievementList.isEmpty();
        if (hasAchievement) {
            score += ACHIEVEMENT_POINTS;
            checks.add(new HealthCheck("Add your achievements", true, ACHIEVEMENT_POINTS));
        } else {
            checks.add(new HealthCheck("Add your achievements", false, ACHIEVEMENT_POINTS));
        }

        return score;
    }

    private int scoreAnalytics(Optional<PortfolioAnalyticsSummary> analyticsOpt, List<HealthCheck> checks) {
        int score = 0;

        if (analyticsOpt.isPresent()) {
            PortfolioAnalyticsSummary analytics = analyticsOpt.get();
            
            // At least 1 view (5 points)
            if (analytics.getTotalViews() >= 1) {
                score += ANALYTICS_VIEW_POINTS;
                checks.add(new HealthCheck("Get at least 1 portfolio view", true, ANALYTICS_VIEW_POINTS));
            } else {
                checks.add(new HealthCheck("Get at least 1 portfolio view", false, ANALYTICS_VIEW_POINTS));
            }

            // Engagement rate ≥ 50% (10 points)
            if (analytics.getTotalViews() > 0) {
                int engagementRate = (analytics.getEngagedViews() * 100) / analytics.getTotalViews();
                if (engagementRate >= ENGAGEMENT_THRESHOLD_PERCENT) {
                    score += ANALYTICS_ENGAGEMENT_POINTS;
                    checks.add(new HealthCheck("Maintain engagement rate ≥ 50%", true, ANALYTICS_ENGAGEMENT_POINTS));
                } else {
                    checks.add(new HealthCheck("Maintain engagement rate ≥ 50%", false, ANALYTICS_ENGAGEMENT_POINTS));
                }
            } else {
                checks.add(new HealthCheck("Maintain engagement rate ≥ 50%", false, ANALYTICS_ENGAGEMENT_POINTS));
            }
        } else {
            checks.add(new HealthCheck("Get at least 1 portfolio view", false, ANALYTICS_VIEW_POINTS));
            checks.add(new HealthCheck("Maintain engagement rate ≥ 50%", false, ANALYTICS_ENGAGEMENT_POINTS));
        }

        return score;
    }

    private int scoreActivity(Optional<Profile> profileOpt, List<Project> projects, List<HealthCheck> checks) {
        int score = 0;
        Instant thirtyDaysAgo = Instant.now().minus(ACTIVITY_DAYS_THRESHOLD, ChronoUnit.DAYS);
        boolean hasRecentActivity = false;

        // Check profile update
        if (profileOpt.isPresent()) {
            Profile profile = profileOpt.get();
            if (profile.getUpdatedAt() != null && profile.getUpdatedAt().isAfter(thirtyDaysAgo)) {
                hasRecentActivity = true;
            }
        }

        // Check project creation/update
        if (!hasRecentActivity && !projects.isEmpty()) {
            for (Project project : projects) {
                Instant checkDate = project.getUpdatedAt() != null ? project.getUpdatedAt() : project.getCreatedAt();
                if (checkDate != null && checkDate.isAfter(thirtyDaysAgo)) {
                    hasRecentActivity = true;
                    break;
                }
            }
        }

        if (hasRecentActivity) {
            score += ACTIVITY_RECENT_POINTS;
            checks.add(new HealthCheck("Update your portfolio in the last 30 days", true, ACTIVITY_RECENT_POINTS));
        } else {
            checks.add(new HealthCheck("Update your portfolio in the last 30 days", false, ACTIVITY_RECENT_POINTS));
        }

        return score;
    }
}

