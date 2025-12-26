package com.developer.analytics;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.analytics.dto.AnalyticsTrackingRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/public/portfolio")
@Validated
public class PublicAnalyticsController {

    private static final String VISITOR_ID_HEADER = "X-Visitor-Id";

    private final PortfolioAnalyticsService analyticsService;

    public PublicAnalyticsController(PortfolioAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Tracks analytics events for a public portfolio.
     * No authentication required. Fails silently to never block portfolio rendering.
     *
     * @param username The username of the portfolio owner
     * @param request The tracking request containing event type and duration
     * @param visitorIdHeader The visitor ID from header (optional, can be in request body)
     * @return 204 No Content on success
     */
    @PostMapping("/{username}/track")
    public ResponseEntity<Void> trackEvent(
            @PathVariable String username,
            @Valid @RequestBody AnalyticsTrackingRequest request,
            @RequestHeader(value = VISITOR_ID_HEADER, required = false) String visitorIdHeader) {

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

        // Track the event (service handles all validation and errors silently)
        analyticsService.trackEvent(username, request, visitorId);

        // Always return 204 to never block portfolio rendering
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

