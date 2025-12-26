package com.developer.analytics.summary.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DashboardAnalyticsResponse {

    private Integer totalViews;
    private Integer engagementRate; // Percentage (0-100)
    private Integer bounceRate; // Percentage (0-100)
    private Integer avgTimeOnPage; // Average duration in seconds
}

