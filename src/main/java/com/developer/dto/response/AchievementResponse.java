package com.developer.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.developer.entity.Achievement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AchievementResponse {

    private UUID id;
    private UUID userId;
    private String title;
    private String issuer;
    private LocalDate issueDate;
    private String description;
    private String link;
    private Instant createdAt;

    public static AchievementResponse fromEntity(Achievement achievement) {
        AchievementResponse response = new AchievementResponse();
        response.setId(achievement.getId());
        response.setUserId(achievement.getUser().getId());
        response.setTitle(achievement.getTitle());
        response.setIssuer(achievement.getIssuer());
        response.setIssueDate(achievement.getIssueDate());
        response.setDescription(achievement.getDescription());
        response.setLink(achievement.getLink());
        response.setCreatedAt(achievement.getCreatedAt());
        return response;
    }
}

