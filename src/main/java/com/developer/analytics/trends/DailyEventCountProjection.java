package com.developer.analytics.trends;

import java.time.LocalDate;

public interface DailyEventCountProjection {
    LocalDate getDate();
    String getEventType();
    Long getCount();
}