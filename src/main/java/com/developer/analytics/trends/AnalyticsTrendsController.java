package com.developer.analytics.trends;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.analytics.trends.dto.AnalyticsTrendsResponse;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.UserRepository;

/**
 * Controller for analytics trends endpoint.
 * Requires authentication - users can only view their own analytics trends.
 */
@RestController
@RequestMapping("/api/dashboard/analytics")
public class AnalyticsTrendsController {

    private final AnalyticsTrendsService trendsService;
    private final UserRepository userRepository;

    public AnalyticsTrendsController(
            AnalyticsTrendsService trendsService,
            UserRepository userRepository) {
        this.trendsService = trendsService;
        this.userRepository = userRepository;
    }

    /**
     * Gets analytics trends for the current authenticated user's portfolio.
     * Returns trends for the last 7 days (including today).
     * 
     * @return Analytics trends response with views, engagement rate, and bounce rate
     */
    @GetMapping("/trends")
    public ResponseEntity<AnalyticsTrendsResponse> getTrends() {
        User currentUser = getCurrentUser();
        AnalyticsTrendsResponse response = trendsService.computeTrends(currentUser.getId());
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
}

