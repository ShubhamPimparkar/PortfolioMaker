package com.developer.analytics.trends.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsTrendPoint {

    private LocalDate date;
    private Integer count; // For views
    private Integer value; // For rates (percentage)
}

