package com.developer.analytics.dto;

import com.developer.analytics.AnalyticsEventType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnalyticsTrackingRequest {

    @NotNull(message = "Event type is required")
    private AnalyticsEventType eventType;

    @Min(value = 0, message = "Duration seconds must be non-negative")
    private Integer durationSeconds;

    // Optional: visitor ID can be sent in request body as fallback
    private String visitorId;
}

