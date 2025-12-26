package com.developer.analytics.trends.dto;

import java.time.LocalDate;

import com.developer.analytics.AnalyticsEventType;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Projection DTO for daily event counts grouped by date and event type.
 */
@Getter
@AllArgsConstructor
public class DailyEventCount {

    private LocalDate date;
    private AnalyticsEventType eventType;
    private Long count;
}

