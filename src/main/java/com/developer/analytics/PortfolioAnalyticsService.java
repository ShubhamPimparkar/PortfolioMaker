package com.developer.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.analytics.dto.AnalyticsTrackingRequest;
import com.developer.entity.User;
import com.developer.repository.UserRepository;

@Service
public class PortfolioAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioAnalyticsService.class);

    private final PortfolioAnalyticsEventRepository analyticsEventRepository;
    private final UserRepository userRepository;

    public PortfolioAnalyticsService(
            PortfolioAnalyticsEventRepository analyticsEventRepository,
            UserRepository userRepository) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.userRepository = userRepository;
    }

    /**
     * Tracks an analytics event for a portfolio.
     * Fails silently on errors to never block portfolio rendering.
     *
     * @param username The username of the portfolio owner
     * @param request The tracking request containing event type and duration
     * @param visitorId The visitor identifier (opaque string from frontend)
     */
    @Transactional
    public void trackEvent(String username, AnalyticsTrackingRequest request, String visitorId) {
        try {
            // Resolve portfolio owner
            User portfolioUser = userRepository.findByUsername(username)
                    .orElse(null);

            if (portfolioUser == null) {
                logger.debug("Portfolio owner not found for username: {}", username);
                return; // Fail silently
            }

            // Validate payload
            if (request == null || request.getEventType() == null) {
                logger.debug("Invalid tracking request for username: {}", username);
                return; // Fail silently
            }

            // Validate duration if provided
            if (request.getDurationSeconds() != null && request.getDurationSeconds() < 0) {
                logger.debug("Invalid duration seconds for username: {}", username);
                return; // Fail silently
            }

            // Validate visitor ID
            if (visitorId == null || visitorId.isBlank()) {
                logger.debug("Missing visitor ID for username: {}", username);
                return; // Fail silently
            }

            // Create and persist event
            PortfolioAnalyticsEvent event = new PortfolioAnalyticsEvent();
            event.setPortfolioUser(portfolioUser);
            event.setVisitorId(visitorId);
            event.setEventType(request.getEventType());
            event.setDurationSeconds(request.getDurationSeconds());

            analyticsEventRepository.save(event);

            logger.debug("Analytics event tracked: {} for portfolio owner: {}", 
                    request.getEventType(), username);

        } catch (Exception e) {
            // Fail silently - log only
            logger.warn("Failed to track analytics event for username: {}. Error: {}", 
                    username, e.getMessage(), e);
        }
    }
}

