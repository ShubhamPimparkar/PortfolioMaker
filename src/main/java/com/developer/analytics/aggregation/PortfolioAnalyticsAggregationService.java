package com.developer.analytics.aggregation;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.analytics.AnalyticsEventType;
import com.developer.analytics.PortfolioAnalyticsEvent;
import com.developer.analytics.PortfolioAnalyticsEventRepository;
import com.developer.analytics.summary.PortfolioAnalyticsSummary;
import com.developer.analytics.summary.PortfolioAnalyticsSummaryRepository;

/**
 * Service responsible for aggregating raw analytics events into summary metrics.
 * 
 * Aggregation logic:
 * - VIEW events count as total views
 * - ENGAGED events count as engaged views (duration >= threshold)
 * - BOUNCE events count as bounce (duration < threshold)
 * - Average duration calculated from all events with duration
 */
@Service
public class PortfolioAnalyticsAggregationService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioAnalyticsAggregationService.class);
    
    // Engagement threshold: 30 seconds
    private static final int ENGAGEMENT_THRESHOLD_SECONDS = 30;

    private final PortfolioAnalyticsEventRepository eventRepository;
    private final PortfolioAnalyticsSummaryRepository summaryRepository;

    public PortfolioAnalyticsAggregationService(
            PortfolioAnalyticsEventRepository eventRepository,
            PortfolioAnalyticsSummaryRepository summaryRepository) {
        this.eventRepository = eventRepository;
        this.summaryRepository = summaryRepository;
    }

    /**
     * Aggregates analytics events for a specific portfolio user and updates the summary.
     * 
     * @param portfolioUserId The UUID of the portfolio owner
     */
    @Transactional
    public void aggregateForUser(UUID portfolioUserId) {
        try {
            // Fetch all events for this user
            List<PortfolioAnalyticsEvent> events = eventRepository.findByPortfolioUserId(portfolioUserId);

            if (events.isEmpty()) {
                // No events yet - create empty summary or reset existing
                createOrUpdateSummary(portfolioUserId, 0, 0, 0, 0);
                logger.debug("No events found for user {}, created empty summary", portfolioUserId);
                return;
            }

            // Compute metrics
            int totalViews = 0;
            int engagedViews = 0;
            int bounceCount = 0;
            int totalDuration = 0;
            int durationCount = 0;

            for (PortfolioAnalyticsEvent event : events) {
                // Count event types
                if (event.getEventType() == AnalyticsEventType.VIEW) {
                    totalViews++;
                } else if (event.getEventType() == AnalyticsEventType.ENGAGED) {
                    engagedViews++;
                } else if (event.getEventType() == AnalyticsEventType.BOUNCE) {
                    bounceCount++;
                }

                // Include duration from any event that has it for average calculation
                if (event.getDurationSeconds() != null) {
                    totalDuration += event.getDurationSeconds();
                    durationCount++;
                }
            }

            // Calculate average duration
            int avgDurationSeconds = durationCount > 0 ? totalDuration / durationCount : 0;

            // Persist or update summary
            createOrUpdateSummary(portfolioUserId, totalViews, engagedViews, bounceCount, avgDurationSeconds);

            logger.debug("Aggregated analytics for user {}: views={}, engaged={}, bounce={}, avgDuration={}s",
                    portfolioUserId, totalViews, engagedViews, bounceCount, avgDurationSeconds);

        } catch (Exception e) {
            logger.error("Failed to aggregate analytics for user {}: {}", portfolioUserId, e.getMessage(), e);
            // Fail silently - don't throw to avoid breaking scheduled jobs
        }
    }

    /**
     * Creates or updates the analytics summary for a user.
     */
    private void createOrUpdateSummary(UUID portfolioUserId, int totalViews, int engagedViews, 
                                      int bounceCount, int avgDurationSeconds) {
        PortfolioAnalyticsSummary summary = summaryRepository.findByPortfolioUserId(portfolioUserId)
                .orElse(new PortfolioAnalyticsSummary());

        summary.setPortfolioUserId(portfolioUserId);
        summary.setTotalViews(totalViews);
        summary.setEngagedViews(engagedViews);
        summary.setBounceCount(bounceCount);
        summary.setAvgDurationSeconds(avgDurationSeconds);

        summaryRepository.save(summary);
    }

    /**
     * Gets the engagement threshold in seconds.
     * Can be made configurable in the future.
     */
    public static int getEngagementThreshold() {
        return ENGAGEMENT_THRESHOLD_SECONDS;
    }
}

