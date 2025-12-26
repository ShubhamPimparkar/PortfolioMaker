package com.developer.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portfolio_settings")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioSettings {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "template_key", nullable = false, length = 50)
    private String templateKey;

    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    @Column(name = "font_family", length = 100)
    private String fontFamily;

    @Column(name = "show_skills", nullable = false)
    private Boolean showSkills = true;

    @Column(name = "show_projects", nullable = false)
    private Boolean showProjects = true;

    @Column(name = "show_education")
    private Boolean showEducation = true;

    @Column(name = "show_achievements")
    private Boolean showAchievements = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

