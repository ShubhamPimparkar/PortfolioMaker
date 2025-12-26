package com.developer.dashboard.overview;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.developer.dashboard.overview.dto.DashboardOverviewResponse;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.UserRepository;

/**
 * Controller for dashboard overview endpoint.
 * Requires authentication - users can only view their own dashboard.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardOverviewController {

    private final DashboardOverviewService overviewService;
    private final UserRepository userRepository;

    public DashboardOverviewController(
            DashboardOverviewService overviewService,
            UserRepository userRepository) {
        this.overviewService = overviewService;
        this.userRepository = userRepository;
    }

    /**
     * Gets the complete dashboard overview for the current authenticated user.
     * 
     * @return Dashboard overview response with user info, health, analytics, and projects
     */
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getOverview() {
        User currentUser = getCurrentUser();
        DashboardOverviewResponse response = overviewService.buildOverview(currentUser);
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

