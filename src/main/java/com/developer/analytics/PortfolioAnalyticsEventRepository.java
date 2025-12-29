package com.developer.analytics;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

    /**
     * Checks if a VIEW event exists for the given visitor and portfolio within the time window.
     * Used for de-duplication: only one VIEW per visitor per portfolio per time window.
     * 
     * @param portfolioUserId The portfolio owner's user ID
     * @param visitorId The visitor identifier
     * @param sinceTime The earliest time to check (e.g., 30 minutes ago)
     * @return true if a VIEW event exists, false otherwise
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM PortfolioAnalyticsEvent e 
            WHERE e.portfolioUser.id = :portfolioUserId 
                AND e.visitorId = :visitorId 
                AND e.eventType = 'VIEW'
                AND e.createdAt >= :sinceTime
            """)
    boolean existsViewEventForVisitorSince(
            @Param("portfolioUserId") UUID portfolioUserId,
            @Param("visitorId") String visitorId,
            @Param("sinceTime") Instant sinceTime);

    /**
     * Checks if an ENGAGED event exists for the given visitor and portfolio.
     * Used to prevent duplicate ENGAGED events per visit.
     * 
     * @param portfolioUserId The portfolio owner's user ID
     * @param visitorId The visitor identifier
     * @param sinceTime The earliest time to check (e.g., 30 minutes ago)
     * @return true if an ENGAGED event exists, false otherwise
     */
    @Query("""
            SELECT COUNT(e) > 0 FROM PortfolioAnalyticsEvent e 
            WHERE e.portfolioUser.id = :portfolioUserId 
                AND e.visitorId = :visitorId 
                AND e.eventType = 'ENGAGED'
                AND e.createdAt >= :sinceTime
            """)
    boolean existsEngagedEventForVisitorSince(
            @Param("portfolioUserId") UUID portfolioUserId,
            @Param("visitorId") String visitorId,
            @Param("sinceTime") Instant sinceTime);

    /**
     * Finds the most recent VIEW event for a visitor and portfolio.
     * Used to validate event flow (ENGAGED must have a corresponding VIEW).
     * 
     * @param portfolioUserId The portfolio owner's user ID
     * @param visitorId The visitor identifier
     * @param sinceTime The earliest time to check (e.g., 30 minutes ago)
     * @return The most recent VIEW event, or empty if none exists
     */
    @Query("""
            SELECT e FROM PortfolioAnalyticsEvent e 
            WHERE e.portfolioUser.id = :portfolioUserId 
                AND e.visitorId = :visitorId 
                AND e.eventType = 'VIEW'
                AND e.createdAt >= :sinceTime
            ORDER BY e.createdAt DESC
            """)
    List<PortfolioAnalyticsEvent> findViewEventsForVisitor(
            @Param("portfolioUserId") UUID portfolioUserId,
            @Param("visitorId") String visitorId,
            @Param("sinceTime") Instant sinceTime);
    
    /**
     * Gets the most recent VIEW event for a visitor (helper method).
     */
    default Optional<PortfolioAnalyticsEvent> findMostRecentViewEventForVisitor(
            UUID portfolioUserId, String visitorId, Instant sinceTime) {
        List<PortfolioAnalyticsEvent> events = findViewEventsForVisitor(
                portfolioUserId, visitorId, sinceTime);
        return events.isEmpty() ? Optional.empty() : Optional.of(events.get(0));
    }
}

