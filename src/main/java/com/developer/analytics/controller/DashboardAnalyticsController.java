package com.developer.analytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.analytics.summary.PortfolioAnalyticsSummary;
import com.developer.analytics.summary.PortfolioAnalyticsSummaryRepository;
import com.developer.analytics.summary.dto.DashboardAnalyticsResponse;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.UserRepository;

/**
 * Controller for dashboard analytics endpoints.
 * Requires authentication - users can only view their own analytics.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardAnalyticsController {

    private final PortfolioAnalyticsSummaryRepository summaryRepository;
    private final UserRepository userRepository;

    public DashboardAnalyticsController(
            PortfolioAnalyticsSummaryRepository summaryRepository,
            UserRepository userRepository) {
        this.summaryRepository = summaryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Gets analytics metrics for the current authenticated user's portfolio.
     * 
     * @return Dashboard analytics response with metrics
     */
    @GetMapping("/analytics")
    public ResponseEntity<DashboardAnalyticsResponse> getAnalytics() {
        User currentUser = getCurrentUser();

        // Fetch summary from database (fast read)
        PortfolioAnalyticsSummary summary = summaryRepository.findByPortfolioUserId(currentUser.getId())
                .orElse(createEmptySummary(currentUser.getId()));

        // Convert to response DTO
        DashboardAnalyticsResponse response = new DashboardAnalyticsResponse();
        response.setTotalViews(summary.getTotalViews());

        // Calculate rates (percentages)
        int totalViews = summary.getTotalViews();
        if (totalViews > 0) {
            int engagementRate = (summary.getEngagedViews() * 100) / totalViews;
            int bounceRate = (summary.getBounceCount() * 100) / totalViews;
            response.setEngagementRate(engagementRate);
            response.setBounceRate(bounceRate);
        } else {
            response.setEngagementRate(0);
            response.setBounceRate(0);
        }

        response.setAvgTimeOnPage(summary.getAvgDurationSeconds());

        return ResponseEntity.ok(response);
    }

    /**
     * Gets the current authenticated user.
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Creates an empty summary for users with no analytics yet.
     */
    private PortfolioAnalyticsSummary createEmptySummary(java.util.UUID userId) {
        PortfolioAnalyticsSummary empty = new PortfolioAnalyticsSummary();
        empty.setPortfolioUserId(userId);
        empty.setTotalViews(0);
        empty.setEngagedViews(0);
        empty.setBounceCount(0);
        empty.setAvgDurationSeconds(0);
        return empty;
    }
}

