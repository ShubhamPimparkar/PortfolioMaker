package com.developer.analytics;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.analytics.dto.AnalyticsTrackingRequest;
import com.developer.entity.User;
import com.developer.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public/portfolio")
@Validated
public class PublicAnalyticsController {

    private static final String VISITOR_ID_HEADER = "X-Visitor-Id";
    private static final String USER_AGENT_HEADER = "User-Agent";

    private final PortfolioAnalyticsService analyticsService;
    private final UserRepository userRepository;

    public PublicAnalyticsController(
            PortfolioAnalyticsService analyticsService,
            UserRepository userRepository) {
        this.analyticsService = analyticsService;
        this.userRepository = userRepository;
    }

    /**
     * Tracks analytics events for a public portfolio.
     * No authentication required, but if authenticated, self-views are excluded.
     * Fails silently to never block portfolio rendering.
     *
     * @param username The username of the portfolio owner
     * @param request The tracking request containing event type, duration, and scroll depth
     * @param visitorIdHeader The visitor ID from header (optional, can be in request body)
     * @param httpRequest The HTTP request to extract User-Agent
     * @return 204 No Content on success
     */
    @PostMapping("/{username}/track")
    public ResponseEntity<Void> trackEvent(
            @PathVariable String username,
            @Valid @RequestBody AnalyticsTrackingRequest request,
            @RequestHeader(value = VISITOR_ID_HEADER, required = false) String visitorIdHeader,
            HttpServletRequest httpRequest) {

        // Extract visitor ID: header > request body > fallback
        // Frontend can send visitor ID in header or request body
        String visitorId = null;
        if (visitorIdHeader != null && !visitorIdHeader.isBlank()) {
            visitorId = visitorIdHeader;
        } else if (request.getVisitorId() != null && !request.getVisitorId().isBlank()) {
            visitorId = request.getVisitorId();
        } else {
            visitorId = "anonymous"; // Fallback for missing visitor ID
        }

        // Extract User-Agent header
        String userAgent = httpRequest.getHeader(USER_AGENT_HEADER);

        // Get authenticated user ID (if any) for self-view exclusion
        UUID authenticatedUserId = getAuthenticatedUserId();

        // Track the event (service handles all validation, filtering, and errors silently)
        analyticsService.trackEvent(username, request, visitorId, authenticatedUserId, userAgent);

        // Always return 204 to never block portfolio rendering
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * Gets the authenticated user's ID if available.
     * Returns null if unauthenticated.
     */
    private UUID getAuthenticatedUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            // Fail silently - return null if unable to determine authenticated user
            return null;
        }
    }
}

