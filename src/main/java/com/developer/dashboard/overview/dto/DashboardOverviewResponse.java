package com.developer.dashboard.overview.dto;

import java.util.List;

import com.developer.dashboard.health.dto.PortfolioHealthResponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardOverviewResponse {

    private UserInfo user;
    private PortfolioHealthResponse portfolioHealth;
    private AnalyticsInfo analytics;
    private ProjectsInfo projects;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserInfo {
        private String name;
        private String email;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class AnalyticsInfo {
        private Integer totalViews;
        private Integer engagementRate;
        private Integer bounceRate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ProjectsInfo {
        private Integer total;
        private List<ProjectItem> recent;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class ProjectItem {
            private java.util.UUID id;
            private String title;
            private java.time.Instant createdAt;
        }
    }
}

