package com.developer.dto.response;

import com.developer.entity.PortfolioSettings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioSettingsResponse {

    private String templateKey;
    private String primaryColor;
    private String fontFamily;
    private Boolean showSkills;
    private Boolean showProjects;
    private Boolean showEducation;
    private Boolean showAchievements;

    public static PortfolioSettingsResponse fromEntity(PortfolioSettings settings) {
        PortfolioSettingsResponse response = new PortfolioSettingsResponse();
        response.setTemplateKey(settings.getTemplateKey());
        response.setPrimaryColor(settings.getPrimaryColor());
        response.setFontFamily(settings.getFontFamily());
        response.setShowSkills(settings.getShowSkills());
        response.setShowProjects(settings.getShowProjects());
        response.setShowEducation(settings.getShowEducation());
        response.setShowAchievements(settings.getShowAchievements());
        return response;
    }
}

