package com.developer.dashboard.overview;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.analytics.summary.PortfolioAnalyticsSummary;
import com.developer.analytics.summary.PortfolioAnalyticsSummaryRepository;
import com.developer.dashboard.health.PortfolioHealthService;
import com.developer.dashboard.health.dto.PortfolioHealthResponse;
import com.developer.dashboard.overview.dto.DashboardOverviewResponse;
import com.developer.entity.Profile;
import com.developer.entity.Project;
import com.developer.entity.User;
import com.developer.repository.ProfileRepository;
import com.developer.repository.ProjectRepository;

/**
 * Service that coordinates dashboard overview data aggregation.
 * Fetches data from multiple sources and assembles the dashboard response.
 */
@Service
public class DashboardOverviewService {

    private static final int RECENT_PROJECTS_LIMIT = 5;

    private final PortfolioHealthService healthService;
    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final PortfolioAnalyticsSummaryRepository analyticsSummaryRepository;

    public DashboardOverviewService(
            PortfolioHealthService healthService,
            ProfileRepository profileRepository,
            ProjectRepository projectRepository,
            PortfolioAnalyticsSummaryRepository analyticsSummaryRepository) {
        this.healthService = healthService;
        this.profileRepository = profileRepository;
        this.projectRepository = projectRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
    }

    /**
     * Builds the complete dashboard overview for a user.
     * 
     * @param user The authenticated user
     * @return DashboardOverviewResponse with all dashboard data
     */
    @Transactional(readOnly = true)
    public DashboardOverviewResponse buildOverview(User user) {
        DashboardOverviewResponse response = new DashboardOverviewResponse();

        // User info
        response.setUser(buildUserInfo(user));

        // Portfolio health
        PortfolioHealthResponse health = healthService.computeHealth(user);
        response.setPortfolioHealth(health);

        // Analytics
        response.setAnalytics(buildAnalyticsInfo(user.getId()));

        // Projects
        response.setProjects(buildProjectsInfo(user));

        return response;
    }

    private DashboardOverviewResponse.UserInfo buildUserInfo(User user) {
        DashboardOverviewResponse.UserInfo userInfo = new DashboardOverviewResponse.UserInfo();
        userInfo.setEmail(user.getEmail());
        
        // Try to get name from profile, fallback to username
        Optional<Profile> profileOpt = profileRepository.findByUser(user);
        if (profileOpt.isPresent() && profileOpt.get().getFullName() != null) {
            userInfo.setName(profileOpt.get().getFullName());
        } else {
            userInfo.setName(user.getUsername());
        }
        
        return userInfo;
    }

    private DashboardOverviewResponse.AnalyticsInfo buildAnalyticsInfo(java.util.UUID userId) {
        DashboardOverviewResponse.AnalyticsInfo analyticsInfo = new DashboardOverviewResponse.AnalyticsInfo();
        
        Optional<PortfolioAnalyticsSummary> summaryOpt = analyticsSummaryRepository.findByPortfolioUserId(userId);
        if (summaryOpt.isPresent()) {
            PortfolioAnalyticsSummary summary = summaryOpt.get();
            analyticsInfo.setTotalViews(summary.getTotalViews());
            
            if (summary.getTotalViews() > 0) {
                int engagementRate = (summary.getEngagedViews() * 100) / summary.getTotalViews();
                int bounceRate = (summary.getBounceCount() * 100) / summary.getTotalViews();
                analyticsInfo.setEngagementRate(engagementRate);
                analyticsInfo.setBounceRate(bounceRate);
            } else {
                analyticsInfo.setEngagementRate(0);
                analyticsInfo.setBounceRate(0);
            }
        } else {
            analyticsInfo.setTotalViews(0);
            analyticsInfo.setEngagementRate(0);
            analyticsInfo.setBounceRate(0);
        }
        
        return analyticsInfo;
    }

    private DashboardOverviewResponse.ProjectsInfo buildProjectsInfo(User user) {
        DashboardOverviewResponse.ProjectsInfo projectsInfo = new DashboardOverviewResponse.ProjectsInfo();
        
        List<Project> allProjects = projectRepository.findByUserOrderByCreatedAtDesc(user);
        projectsInfo.setTotal(allProjects.size());
        
        // Get recent projects (limit to 5)
        List<DashboardOverviewResponse.ProjectsInfo.ProjectItem> recentProjects = allProjects.stream()
                .limit(RECENT_PROJECTS_LIMIT)
                .map(project -> {
                    DashboardOverviewResponse.ProjectsInfo.ProjectItem item = 
                            new DashboardOverviewResponse.ProjectsInfo.ProjectItem();
                    item.setId(project.getId());
                    item.setTitle(project.getTitle());
                    item.setCreatedAt(project.getCreatedAt());
                    return item;
                })
                .collect(Collectors.toList());
        
        projectsInfo.setRecent(recentProjects);
        
        return projectsInfo;
    }
}

