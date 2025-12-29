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
 * - De-duplicates repeat views (30-minute window per visitor)
 * - Filters bots and crawlers by User-Agent
 * - Validates event flow (ENGAGED must have a corresponding VIEW)
 * - Validates engagement conditions (duration >= 30s OR scroll >= 50%)
 * - Filters low-quality ENGAGED events (duration < 2 seconds)
 * 
 * Key behavior:
 * - VIEW events: Tracked immediately on page load (duration can be 0)
 * - ENGAGED events: Only tracked if engagement criteria are met and a VIEW exists
 * - All validation failures are silent to never block portfolio rendering
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
            // STEP 1: VALIDATE PAYLOAD
            if (request == null || request.getEventType() == null) {
                logger.debug("Invalid tracking request: missing event type for username: {}", username);
                return; // Fail silently
            }

            AnalyticsEventType eventType = request.getEventType();

            // Validate duration if provided (must be non-negative)
            if (request.getDurationSeconds() != null && request.getDurationSeconds() < 0) {
                logger.debug("Invalid duration seconds (negative) for username: {}", username);
                return; // Fail silently
            }

            // Validate scroll depth if provided (must be 0-100)
            if (request.getScrollDepth() != null && 
                (request.getScrollDepth() < 0 || request.getScrollDepth() > 100)) {
                logger.debug("Invalid scroll depth (out of range) for username: {}", username);
                return; // Fail silently
            }

            // Validate visitor ID
            if (visitorId == null || visitorId.isBlank() || "anonymous".equals(visitorId)) {
                logger.debug("Missing or invalid visitor ID for username: {}", username);
                return; // Fail silently
            }

            // STEP 2: RESOLVE PORTFOLIO OWNER
            User portfolioUser = userRepository.findByUsername(username)
                    .orElse(null);

            if (portfolioUser == null) {
                logger.debug("Portfolio owner not found for username: {}", username);
                return; // Fail silently
            }

            // STEP 3: SELF-VIEW EXCLUSION
            // If authenticated user is viewing their own portfolio, ignore the event
            if (authenticatedUserId != null && authenticatedUserId.equals(portfolioUser.getId())) {
                logger.debug("Self-view excluded for portfolio owner: {} (visitor: {})", username, visitorId);
                return; // Fail silently
            }

            // STEP 4: BOT FILTERING (User-Agent based)
            // Check for bots/crawlers - this applies to all event types
            if (isBot(userAgent)) {
                logger.debug("Bot/crawler filtered for username: {} (user-agent: {})", username, userAgent);
                return; // Fail silently
            }

            // Calculate de-duplication window
            Instant sinceTime = Instant.now().minus(DEDUPLICATION_WINDOW_MINUTES, ChronoUnit.MINUTES);

            // STEP 5: EVENT-SPECIFIC VALIDATION AND DE-DUPLICATION
            if (eventType == AnalyticsEventType.VIEW) {
                // VIEW events: Track immediately on page load (duration can be 0)
                // Only filter if it's a duplicate within the time window
                if (analyticsEventRepository.existsViewEventForVisitorSince(
                        portfolioUser.getId(), visitorId, sinceTime)) {
                    logger.debug("Duplicate VIEW filtered for visitor: {} on portfolio: {} (within {} minutes)", 
                            visitorId, username, DEDUPLICATION_WINDOW_MINUTES);
                    return; // Fail silently
                }
                
                // VIEW events are always valid - no duration requirement
                // They represent a page visit, even if duration is 0
                
            } else if (eventType == AnalyticsEventType.ENGAGED) {
                // ENGAGED events: Must have a corresponding VIEW first
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

                // Validate engagement criteria
                // ENGAGED must meet at least one of:
                // - Duration >= 30 seconds, OR
                // - Scroll depth >= 50%
                boolean meetsEngagementCriteria = false;
                
                if (request.getDurationSeconds() != null && 
                    request.getDurationSeconds() >= ENGAGEMENT_THRESHOLD_SECONDS) {
                    meetsEngagementCriteria = true;
                }
                
                if (!meetsEngagementCriteria && request.getScrollDepth() != null && 
                    request.getScrollDepth() >= ENGAGEMENT_SCROLL_DEPTH_PERCENT) {
                    meetsEngagementCriteria = true;
                }

                if (!meetsEngagementCriteria) {
                    logger.debug("ENGAGED event does not meet engagement criteria (duration: {}, scroll: {}) for visitor: {} on portfolio: {}", 
                            request.getDurationSeconds(), request.getScrollDepth(), visitorId, username);
                    return; // Fail silently
                }

                // Filter low-quality ENGAGED events (duration < 2 seconds is likely noise)
                // This only applies to ENGAGED events, not VIEW events
                if (request.getDurationSeconds() != null && 
                    request.getDurationSeconds() < MIN_DURATION_SECONDS) {
                    logger.debug("Low-quality ENGAGED event filtered (duration < {}s) for visitor: {} on portfolio: {}", 
                            MIN_DURATION_SECONDS, visitorId, username);
                    return; // Fail silently
                }
            }

            // STEP 6: CREATE AND PERSIST EVENT
            PortfolioAnalyticsEvent event = new PortfolioAnalyticsEvent();
            event.setPortfolioUser(portfolioUser);
            event.setVisitorId(visitorId);
            event.setEventType(eventType);
            event.setDurationSeconds(request.getDurationSeconds());
            event.setScrollDepth(request.getScrollDepth());
            event.setUserAgent(userAgent != null && userAgent.length() > 512 
                    ? userAgent.substring(0, 512) : userAgent);

            analyticsEventRepository.save(event);

            logger.debug("Analytics event tracked successfully: {} for portfolio owner: {} (visitor: {}, duration: {}s, scroll: {}%)", 
                    eventType, username, visitorId, 
                    request.getDurationSeconds() != null ? request.getDurationSeconds() : "null",
                    request.getScrollDepth() != null ? request.getScrollDepth() : "null");

        } catch (Exception e) {
            // Fail silently - log only
            logger.warn("Failed to track analytics event for username: {} (event: {}). Error: {}", 
                    username, request != null ? request.getEventType() : "null", e.getMessage(), e);
        }
    }

    /**
     * Checks if the request is from a bot or crawler based on User-Agent.
     * This method only checks User-Agent patterns, not duration.
     * Duration-based filtering is handled separately for ENGAGED events.
     * 
     * @param userAgent The User-Agent header
     * @return true if the request is from a bot/crawler and should be filtered out
     */
    private boolean isBot(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            // Missing User-Agent might indicate a bot, but we'll allow it
            // to avoid false positives (some legitimate clients don't send User-Agent)
            return false;
        }

        String userAgentLower = userAgent.toLowerCase();
        String[] botPatterns = {
            "bot", "crawler", "spider", "scraper",
            "preview", "facebookexternalhit", "twitterbot",
            "linkedinbot", "whatsapp", "telegram", "slackbot",
            "googlebot", "bingbot", "yandexbot", "baiduspider",
            "headless", "phantom", "selenium", "webdriver",
            "curl", "wget", "python-requests", "java/"
        };
        
        for (String pattern : botPatterns) {
            if (userAgentLower.contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}

