package com.developer.service;

import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.request.PortfolioSettingsRequest;
import com.developer.dto.response.PortfolioSettingsResponse;
import com.developer.entity.PortfolioSettings;
import com.developer.entity.User;
import com.developer.exception.InvalidTemplateException;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.PortfolioSettingsRepository;
import com.developer.repository.UserRepository;

@Service
public class PortfolioSettingsService {

    private static final Set<String> ALLOWED_TEMPLATES = Set.of("classic", "modern", "minimal","hero","product","creator");
    private static final String DEFAULT_TEMPLATE = "classic";

    private final PortfolioSettingsRepository portfolioSettingsRepository;
    private final UserRepository userRepository;

    public PortfolioSettingsService(PortfolioSettingsRepository portfolioSettingsRepository,
                                    UserRepository userRepository) {
        this.portfolioSettingsRepository = portfolioSettingsRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public PortfolioSettingsResponse getCurrentUserSettings() {
        User user = getCurrentUser();
        PortfolioSettings settings = portfolioSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));
        return PortfolioSettingsResponse.fromEntity(settings);
    }

    @Transactional
    public PortfolioSettingsResponse updateSettings(PortfolioSettingsRequest request) {
        User user = getCurrentUser();
        
        // Validate template key
        if (!ALLOWED_TEMPLATES.contains(request.getTemplateKey())) {
            throw new InvalidTemplateException(
                    "Invalid template key. Allowed values: " + String.join(", ", ALLOWED_TEMPLATES)
            );
        }

        PortfolioSettings settings = portfolioSettingsRepository.findByUser(user)
                .orElseGet(() -> {
                    PortfolioSettings newSettings = new PortfolioSettings();
                    newSettings.setUser(user);
                    return newSettings;
                });

        settings.setTemplateKey(request.getTemplateKey());
        
        if (request.getPrimaryColor() != null) {
            String primaryColor = request.getPrimaryColor().trim();
            settings.setPrimaryColor(primaryColor.isEmpty() ? null : primaryColor);
        }
        
        if (request.getFontFamily() != null) {
            String fontFamily = request.getFontFamily().trim();
            settings.setFontFamily(fontFamily.isEmpty() ? null : fontFamily);
        }
        
        if (request.getShowSkills() != null) {
            settings.setShowSkills(request.getShowSkills());
        }
        
        if (request.getShowProjects() != null) {
            settings.setShowProjects(request.getShowProjects());
        }
        
        if (request.getShowEducation() != null) {
            settings.setShowEducation(request.getShowEducation());
        }
        
        if (request.getShowAchievements() != null) {
            settings.setShowAchievements(request.getShowAchievements());
        }

        PortfolioSettings saved = portfolioSettingsRepository.save(settings);
        return PortfolioSettingsResponse.fromEntity(saved);
    }

    @Transactional
    public PortfolioSettings createDefaultSettings(User user) {
        PortfolioSettings settings = new PortfolioSettings();
        settings.setUser(user);
        settings.setTemplateKey(DEFAULT_TEMPLATE);
        settings.setShowSkills(true);
        settings.setShowProjects(true);
        settings.setShowEducation(true);
        settings.setShowAchievements(true);
        return portfolioSettingsRepository.save(settings);
    }

    @Transactional(readOnly = true)
    public PortfolioSettings getSettingsByUser(User user) {
        return portfolioSettingsRepository.findByUser(user)
                .orElseGet(() -> createDefaultSettings(user));
    }
}

