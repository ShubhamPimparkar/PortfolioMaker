package com.developer.analytics.summary;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portfolio_analytics_summary")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioAnalyticsSummary {

    @Id
    @Column(name = "portfolio_user_id", nullable = false, updatable = false)
    private UUID portfolioUserId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portfolio_user_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private com.developer.entity.User portfolioUser;

    @Column(name = "total_views", nullable = false)
    private Integer totalViews = 0;

    @Column(name = "engaged_views", nullable = false)
    private Integer engagedViews = 0;

    @Column(name = "bounce_count", nullable = false)
    private Integer bounceCount = 0;

    @Column(name = "avg_duration_seconds", nullable = false)
    private Integer avgDurationSeconds = 0;

    @UpdateTimestamp
    @Column(name = "last_calculated_at", nullable = false)
    private Instant lastCalculatedAt;
}

