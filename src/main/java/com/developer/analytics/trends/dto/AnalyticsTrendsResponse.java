package com.developer.analytics.trends.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnalyticsTrendsResponse {

    private List<AnalyticsTrendPoint> views;
    private List<AnalyticsTrendPoint> engagementRate;
    private List<AnalyticsTrendPoint> bounceRate;
}

