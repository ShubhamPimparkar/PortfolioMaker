package com.developer.analytics.trends;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.developer.analytics.AnalyticsEventType;
import com.developer.analytics.PortfolioAnalyticsEventRepository;
import com.developer.analytics.trends.dto.AnalyticsTrendPoint;
import com.developer.analytics.trends.dto.AnalyticsTrendsResponse;

/**
 * Service responsible for computing analytics trends over time.
 * 
 * Default time window: Last 7 days (including today)
 */
@Service
public class AnalyticsTrendsService {

    private static final int DEFAULT_DAYS = 7;
    private static final ZoneId SERVER_TIMEZONE = ZoneId.systemDefault();

    private final PortfolioAnalyticsEventRepository eventRepository;

    public AnalyticsTrendsService(PortfolioAnalyticsEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Computes analytics trends for a user over the last 7 days.
     * 
     * @param userId The UUID of the portfolio owner
     * @return AnalyticsTrendsResponse with views, engagement rate, and bounce rate trends
     */
    @Transactional(readOnly = true)
    public AnalyticsTrendsResponse computeTrends(UUID userId) {
        // Calculate date range (last 7 days including today)
        LocalDate today = LocalDate.now(SERVER_TIMEZONE);
        LocalDate startDate = today.minusDays(DEFAULT_DAYS - 1); // Include today, so minus 6 days
        
        Instant startInstant = startDate.atStartOfDay(SERVER_TIMEZONE).toInstant();
        Instant endInstant = today.plusDays(1).atStartOfDay(SERVER_TIMEZONE).toInstant();

        // Fetch events grouped by date and event type
        List<DailyEventCountProjection> rawResults = eventRepository.findDailyEventCountsByUserAndDateRange(
                userId, startInstant, endInstant);

        // Process results into a map: date -> eventType -> count
        Map<LocalDate, Map<AnalyticsEventType, Long>> dailyCounts = new HashMap<>();
        for (DailyEventCountProjection row : rawResults) {
            LocalDate date = row.getDate();
            AnalyticsEventType eventType = AnalyticsEventType.valueOf(row.getEventType());
            Long count = row.getCount();
            dailyCounts.computeIfAbsent(date, k -> new HashMap<>()).put(eventType, count);
        }

        // Generate all dates in range (including missing ones)
        List<LocalDate> allDates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(today)) {
            allDates.add(current);
            current = current.plusDays(1);
        }

        // Build trends
        List<AnalyticsTrendPoint> views = buildViewsTrend(allDates, dailyCounts);
        List<AnalyticsTrendPoint> engagementRate = buildEngagementRateTrend(allDates, dailyCounts);
        List<AnalyticsTrendPoint> bounceRate = buildBounceRateTrend(allDates, dailyCounts);

        AnalyticsTrendsResponse response = new AnalyticsTrendsResponse();
        response.setViews(views);
        response.setEngagementRate(engagementRate);
        response.setBounceRate(bounceRate);

        return response;
    }

    private List<AnalyticsTrendPoint> buildViewsTrend(
            List<LocalDate> dates, Map<LocalDate, Map<AnalyticsEventType, Long>> dailyCounts) {
        List<AnalyticsTrendPoint> trend = new ArrayList<>();
        
        for (LocalDate date : dates) {
            Map<AnalyticsEventType, Long> dayCounts = dailyCounts.getOrDefault(date, new HashMap<>());
            Long viewCount = dayCounts.getOrDefault(AnalyticsEventType.VIEW, 0L);
            
            AnalyticsTrendPoint point = new AnalyticsTrendPoint();
            point.setDate(date);
            point.setCount(viewCount.intValue());
            trend.add(point);
        }
        
        return trend;
    }

    private List<AnalyticsTrendPoint> buildEngagementRateTrend(
            List<LocalDate> dates, Map<LocalDate, Map<AnalyticsEventType, Long>> dailyCounts) {
        List<AnalyticsTrendPoint> trend = new ArrayList<>();
        
        for (LocalDate date : dates) {
            Map<AnalyticsEventType, Long> dayCounts = dailyCounts.getOrDefault(date, new HashMap<>());
            Long viewCount = dayCounts.getOrDefault(AnalyticsEventType.VIEW, 0L);
            Long engagedCount = dayCounts.getOrDefault(AnalyticsEventType.ENGAGED, 0L);
            
            int rate = 0;
            if (viewCount > 0) {
                rate = (int) Math.round((engagedCount.doubleValue() / viewCount.doubleValue()) * 100);
            }
            
            AnalyticsTrendPoint point = new AnalyticsTrendPoint();
            point.setDate(date);
            point.setValue(rate);
            trend.add(point);
        }
        
        return trend;
    }

    private List<AnalyticsTrendPoint> buildBounceRateTrend(
            List<LocalDate> dates, Map<LocalDate, Map<AnalyticsEventType, Long>> dailyCounts) {
        List<AnalyticsTrendPoint> trend = new ArrayList<>();
        
        for (LocalDate date : dates) {
            Map<AnalyticsEventType, Long> dayCounts = dailyCounts.getOrDefault(date, new HashMap<>());
            Long viewCount = dayCounts.getOrDefault(AnalyticsEventType.VIEW, 0L);
            Long bounceCount = dayCounts.getOrDefault(AnalyticsEventType.BOUNCE, 0L);
            
            int rate = 0;
            if (viewCount > 0) {
                rate = (int) Math.round((bounceCount.doubleValue() / viewCount.doubleValue()) * 100);
            }
            
            AnalyticsTrendPoint point = new AnalyticsTrendPoint();
            point.setDate(date);
            point.setValue(rate);
            trend.add(point);
        }
        
        return trend;
    }
}

