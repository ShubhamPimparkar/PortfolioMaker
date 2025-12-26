package com.developer.analytics;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.developer.analytics.trends.DailyEventCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PortfolioAnalyticsEventRepository extends JpaRepository<PortfolioAnalyticsEvent, UUID> {

    @Query("SELECT e FROM PortfolioAnalyticsEvent e WHERE e.portfolioUser.id = :userId")
    List<PortfolioAnalyticsEvent> findByPortfolioUserId(@Param("userId") UUID userId);

    @Query("SELECT DISTINCT e.portfolioUser.id FROM PortfolioAnalyticsEvent e")
    List<UUID> findDistinctPortfolioUserIds();

    @Query("SELECT e FROM PortfolioAnalyticsEvent e WHERE e.portfolioUser.id = :userId AND e.createdAt >= :startDate AND e.createdAt < :endDate ORDER BY e.createdAt ASC")
    List<PortfolioAnalyticsEvent> findByPortfolioUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    @Query(value = """
            SELECT 
                DATE(e.created_at) as date,
                e.event_type as eventType,
                COUNT(*) as count
            FROM portfolio_analytics_event e
            WHERE e.portfolio_user_id = :userId
                AND e.created_at >= :startDate
                AND e.created_at < :endDate
            GROUP BY DATE(e.created_at), e.event_type
            ORDER BY DATE(e.created_at) ASC
            """, nativeQuery = true)
    List<DailyEventCountProjection> findDailyEventCountsByUserAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}

