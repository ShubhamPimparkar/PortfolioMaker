package com.developer.analytics.aggregation;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.developer.analytics.PortfolioAnalyticsEventRepository;

@Component
public class PortfolioAnalyticsScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioAnalyticsScheduler.class);

    private final PortfolioAnalyticsEventRepository eventRepository;
    private final PortfolioAnalyticsAggregationService aggregationService;

    public PortfolioAnalyticsScheduler(
            PortfolioAnalyticsEventRepository eventRepository,
            PortfolioAnalyticsAggregationService aggregationService) {
        this.eventRepository = eventRepository;
        this.aggregationService = aggregationService;
    }

    @Scheduled(cron = "0 */15 * * * ?")
    public void aggregateAnalytics() {
        logger.info("Starting scheduled analytics aggregation");

        try {
            List<UUID> portfolioUserIds = eventRepository.findDistinctPortfolioUserIds();

            if (portfolioUserIds.isEmpty()) {
                logger.debug("No analytics events found, skipping aggregation");
                return;
            }

            logger.info("Aggregating analytics for {} portfolio(s)", portfolioUserIds.size());

            int successCount = 0;
            for (UUID userId : portfolioUserIds) {
                try {
                    aggregationService.aggregateForUser(userId);
                    successCount++;
                } catch (Exception e) {
                    logger.warn("Failed to aggregate analytics for user {}: {}", userId, e.getMessage());
                }
            }

            logger.info("Completed analytics aggregation: {}/{} successful", successCount, portfolioUserIds.size());

        } catch (Exception e) {
            logger.error("Error during scheduled analytics aggregation: {}", e.getMessage(), e);
        }
    }
}
