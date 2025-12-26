package com.developer.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.developer.entity.Profile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileResponse {

    private UUID id;
    private String fullName;
    private String headline;
    private String summary;
    private String location;
    private Integer yearsOfExperience;
    private List<String> skills;
    private String githubUrl;
    private String linkedinUrl;
    private String portfolioUrl;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}


