package com.developer.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.dto.request.ProfileRequest;
import com.developer.dto.response.ProfileResponse;
import com.developer.entity.Profile;
import com.developer.entity.User;
import com.developer.exception.ResourceNotFoundException;
import com.developer.exception.UnauthorizedException;
import com.developer.repository.ProfileRepository;
import com.developer.repository.UserRepository;

@Service
public class ProfileService {

    private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?github\\.com/[a-zA-Z0-9]([a-zA-Z0-9-]*[a-zA-Z0-9])?/?$"
    );
    private static final Pattern LINKEDIN_URL_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?linkedin\\.com/in/[a-zA-Z0-9-]+/?$"
    );

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public ProfileService(ProfileRepository profileRepository, UserRepository userRepository) {
        this.profileRepository = profileRepository;
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

    public ProfileResponse getCurrentUserProfile() {
        User user = getCurrentUser();
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
        return fromEntity(profile);
    }

    public ProfileResponse fromEntity(Profile profile) {
        ProfileResponse response = new ProfileResponse();
        response.setId(profile.getId());
        response.setFullName(profile.getFullName());
        response.setHeadline(profile.getHeadline());
        response.setSummary(profile.getSummary());
        response.setLocation(profile.getLocation());
        response.setYearsOfExperience(profile.getYearsOfExperience());
        response.setSkills(
                profile.getSkills() != null
                        ? List.copyOf(profile.getSkills())
                        : List.of()
        );
        response.setGithubUrl(profile.getGithubUrl());
        response.setLinkedinUrl(profile.getLinkedinUrl());
        response.setPortfolioUrl(profile.getPortfolioUrl());
        if (profile.getUser() != null) {
            response.setEmail(profile.getUser().getEmail());
        }
        response.setCreatedAt(profile.getCreatedAt());
        response.setUpdatedAt(profile.getUpdatedAt());
        return response;
    }

    @Transactional
    public ProfileResponse createProfile(ProfileRequest request) {
        User user = getCurrentUser();

        // Check if profile already exists
        if (profileRepository.findByUser(user).isPresent()) {
            throw new IllegalStateException("Profile already exists. Use PUT to update.");
        }

        validateUrls(request.getGithubUrl(), request.getLinkedinUrl());

        Profile profile = new Profile();
        profile.setUser(user);
        profile.setFullName(request.getFullName());
        profile.setHeadline(request.getHeadline());
        profile.setSummary(request.getSummary());
        profile.setLocation(request.getLocation());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setSkills(request.getSkills());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setPortfolioUrl(request.getPortfolioUrl());

        Profile saved = profileRepository.save(profile);
        return fromEntity(saved);
    }

    @Transactional
    public ProfileResponse updateProfile(ProfileRequest request) {
        User user = getCurrentUser();
        Profile profile = profileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        validateUrls(request.getGithubUrl(), request.getLinkedinUrl());

        profile.setFullName(request.getFullName());
        profile.setHeadline(request.getHeadline());
        profile.setSummary(request.getSummary());
        profile.setLocation(request.getLocation());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setSkills(request.getSkills());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
        profile.setPortfolioUrl(request.getPortfolioUrl());

        Profile saved = profileRepository.save(profile);
        return fromEntity(saved);
    }

    private void validateUrls(String githubUrl, String linkedinUrl) {
        if (githubUrl != null && !githubUrl.isBlank() && !GITHUB_URL_PATTERN.matcher(githubUrl).matches()) {
            throw new IllegalArgumentException("Invalid GitHub URL format");
        }
        if (linkedinUrl != null && !linkedinUrl.isBlank() && !LINKEDIN_URL_PATTERN.matcher(linkedinUrl).matches()) {
            throw new IllegalArgumentException("Invalid LinkedIn URL format");
        }
    }
}

