package com.developer.analytics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.analytics.dto.AnalyticsTrackingRequest;
import com.developer.entity.User;
import com.developer.repository.UserRepository;

/**
 * Service for tracking portfolio analytics events with production-grade filtering:
 * - Excludes self-views (portfolio owner viewing their own portfolio)
 * - De-duplicates repeat views (30-minute window)
 * - Filters bots and crawlers
 * - Validates event flow (VIEW before ENGAGED)
 * - Validates engagement conditions
 * - Filters low-quality visits (duration < 2 seconds)
 */
@Service
public class PortfolioAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioAnalyticsService.class);

    // De-duplication window: 30 minutes
    private static final int DEDUPLICATION_WINDOW_MINUTES = 30;
    
    // Engagement threshold: 30 seconds
    private static final int ENGAGEMENT_THRESHOLD_SECONDS = 30;
    
    // Minimum scroll depth for engagement: 50%
    private static final int ENGAGEMENT_SCROLL_DEPTH_PERCENT = 50;
    
    // Minimum duration to avoid noise: 2 seconds
    private static final int MIN_DURATION_SECONDS = 2;

    private final PortfolioAnalyticsEventRepository analyticsEventRepository;
    private final UserRepository userRepository;

    public PortfolioAnalyticsService(
            PortfolioAnalyticsEventRepository analyticsEventRepository,
            UserRepository userRepository) {
        this.analyticsEventRepository = analyticsEventRepository;
        this.userRepository = userRepository;
    }

    /**
     * Tracks an analytics event for a portfolio with comprehensive filtering.
     * Fails silently on errors to never block portfolio rendering.
     *
     * @param username The username of the portfolio owner
     * @param request The tracking request containing event type, duration, and scroll depth
     * @param visitorId The visitor identifier (opaque string from frontend)
     * @param authenticatedUserId The ID of the authenticated user (null if unauthenticated)
     * @param userAgent The User-Agent header from the request
     */
    @Transactional
    public void trackEvent(String username, AnalyticsTrackingRequest request, String visitorId,
                          UUID authenticatedUserId, String userAgent) {
        try {
            // Resolve portfolio owner
            User portfolioUser = userRepository.findByUsername(username)
                    .orElse(null);

            if (portfolioUser == null) {
                logger.debug("Portfolio owner not found for username: {}", username);
                return; // Fail silently
            }

            // 1. SELF-VIEW EXCLUSION
            // If authenticated user is viewing their own portfolio, ignore the event
            if (authenticatedUserId != null && authenticatedUserId.equals(portfolioUser.getId())) {
                logger.debug("Self-view excluded for portfolio owner: {}", username);
                return; // Fail silently
            }

            // 2. VALIDATE PAYLOAD
            if (request == null || request.getEventType() == null) {
                logger.debug("Invalid tracking request for username: {}", username);
                return; // Fail silently
            }

            // Validate duration if provided
            if (request.getDurationSeconds() != null && request.getDurationSeconds() < 0) {
                logger.debug("Invalid duration seconds for username: {}", username);
                return; // Fail silently
            }

            // Validate scroll depth if provided
            if (request.getScrollDepth() != null && 
                (request.getScrollDepth() < 0 || request.getScrollDepth() > 100)) {
                logger.debug("Invalid scroll depth for username: {}", username);
                return; // Fail silently
            }

            // Validate visitor ID
            if (visitorId == null || visitorId.isBlank()) {
                logger.debug("Missing visitor ID for username: {}", username);
                return; // Fail silently
            }

            // 3. BOT & NOISE FILTERING
            if (isBotOrNoise(userAgent, request.getDurationSeconds())) {
                logger.debug("Bot or noise filtered for username: {}", username);
                return; // Fail silently
            }

            // Calculate de-duplication window
            Instant sinceTime = Instant.now().minus(DEDUPLICATION_WINDOW_MINUTES, ChronoUnit.MINUTES);

            // 4. DE-DUPLICATION LOGIC
            if (request.getEventType() == AnalyticsEventType.VIEW) {
                // Check if a VIEW already exists for this visitor within the time window
                if (analyticsEventRepository.existsViewEventForVisitorSince(
                        portfolioUser.getId(), visitorId, sinceTime)) {
                    logger.debug("Duplicate VIEW filtered for visitor: {} on portfolio: {}", 
                            visitorId, username);
                    return; // Fail silently
                }
            } else if (request.getEventType() == AnalyticsEventType.ENGAGED) {
                // 5. EVENT FLOW VALIDATION
                // ENGAGED must have a corresponding VIEW
                var recentViewOptional = analyticsEventRepository
                        .findMostRecentViewEventForVisitor(portfolioUser.getId(), visitorId, sinceTime);
                
                if (recentViewOptional.isEmpty()) {
                    logger.debug("ENGAGED event without VIEW filtered for visitor: {} on portfolio: {}", 
                            visitorId, username);
                    return; // Fail silently
                }

                // Check for duplicate ENGAGED events
                if (analyticsEventRepository.existsEngagedEventForVisitorSince(
                        portfolioUser.getId(), visitorId, sinceTime)) {
                    logger.debug("Duplicate ENGAGED filtered for visitor: {} on portfolio: {}", 
                            visitorId, username);
                    return; // Fail silently
                }

                // 6. ENGAGEMENT VALIDATION
                // Count as ENGAGED only if:
                // - Duration >= 30 seconds, OR
                // - Scroll depth >= 50%, OR
                // - (Frontend should send ENGAGED only when these conditions are met)
                // We validate here as a safety check
                boolean isEngaged = false;
                if (request.getDurationSeconds() != null && 
                    request.getDurationSeconds() >= ENGAGEMENT_THRESHOLD_SECONDS) {
                    isEngaged = true;
                } else if (request.getScrollDepth() != null && 
                          request.getScrollDepth() >= ENGAGEMENT_SCROLL_DEPTH_PERCENT) {
                    isEngaged = true;
                }

                if (!isEngaged) {
                    logger.debug("ENGAGED event does not meet engagement criteria for visitor: {} on portfolio: {}", 
                            visitorId, username);
                    return; // Fail silently
                }
            }

            // 7. CREATE AND PERSIST EVENT
            PortfolioAnalyticsEvent event = new PortfolioAnalyticsEvent();
            event.setPortfolioUser(portfolioUser);
            event.setVisitorId(visitorId);
            event.setEventType(request.getEventType());
            event.setDurationSeconds(request.getDurationSeconds());
            event.setScrollDepth(request.getScrollDepth());
            event.setUserAgent(userAgent != null && userAgent.length() > 512 
                    ? userAgent.substring(0, 512) : userAgent);

            analyticsEventRepository.save(event);

            logger.debug("Analytics event tracked: {} for portfolio owner: {} (visitor: {})", 
                    request.getEventType(), username, visitorId);

        } catch (Exception e) {
            // Fail silently - log only
            logger.warn("Failed to track analytics event for username: {}. Error: {}", 
                    username, e.getMessage(), e);
        }
    }

    /**
     * Checks if the request is from a bot, crawler, or low-quality visit.
     * 
     * @param userAgent The User-Agent header
     * @param durationSeconds The duration of the visit
     * @return true if the request should be filtered out
     */
    private boolean isBotOrNoise(String userAgent, Integer durationSeconds) {
        // Filter bots and crawlers by User-Agent
        if (userAgent != null && !userAgent.isBlank()) {
            String userAgentLower = userAgent.toLowerCase();
            String[] botPatterns = {
                "bot", "crawler", "spider", "scraper",
                "preview", "facebookexternalhit", "twitterbot",
                "linkedinbot", "whatsapp", "telegram", "slackbot",
                "googlebot", "bingbot", "yandexbot", "baiduspider"
            };
            
            for (String pattern : botPatterns) {
                if (userAgentLower.contains(pattern)) {
                    return true;
                }
            }
        }

        // Filter visits with duration < 2 seconds (likely noise)
        if (durationSeconds != null && durationSeconds < MIN_DURATION_SECONDS) {
            return true;
        }

        return false;
    }
}

