package com.developer.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.request.AchievementRequest;
import com.developer.dto.response.AchievementResponse;
import com.developer.entity.Achievement;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.AchievementRepository;
import com.developer.repository.UserRepository;

@Service
public class AchievementService {

    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;

    public AchievementService(AchievementRepository achievementRepository, UserRepository userRepository) {
        this.achievementRepository = achievementRepository;
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

    @Transactional
    public AchievementResponse createAchievement(AchievementRequest request) {
        User user = getCurrentUser();

        validateAchievementLink(request.getLink());

        Achievement achievement = new Achievement();
        achievement.setUser(user);
        achievement.setTitle(request.getTitle());
        achievement.setIssuer(request.getIssuer());
        achievement.setIssueDate(request.getIssueDate());
        achievement.setDescription(request.getDescription());
        achievement.setLink(request.getLink());

        Achievement saved = achievementRepository.save(achievement);
        return AchievementResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<AchievementResponse> getCurrentUserAchievements() {
        User user = getCurrentUser();
        return achievementRepository.findByUserOrderByIssueDateDescCreatedAtDesc(user)
                .stream()
                .map(AchievementResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AchievementResponse updateAchievement(UUID achievementId, AchievementRequest request) {
        User user = getCurrentUser();
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        // Ownership check
        if (!achievement.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to update this achievement");
        }

        validateAchievementLink(request.getLink());

        achievement.setTitle(request.getTitle());
        achievement.setIssuer(request.getIssuer());
        achievement.setIssueDate(request.getIssueDate());
        achievement.setDescription(request.getDescription());
        achievement.setLink(request.getLink());

        Achievement saved = achievementRepository.save(achievement);
        return AchievementResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteAchievement(UUID achievementId) {
        User user = getCurrentUser();
        Achievement achievement = achievementRepository.findById(achievementId)
                .orElseThrow(() -> new ResourceNotFoundException("Achievement not found"));

        // Ownership check
        if (!achievement.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this achievement");
        }

        achievementRepository.delete(achievement);
    }

    private void validateAchievementLink(String link) {
        if (link != null && !link.isBlank()) {
            try {
                java.net.URI.create(link).toURL();
            } catch (java.net.MalformedURLException | IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid achievement link URL format");
            }
        }
    }
}

