package com.developer.dashboard.health.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioHealthResponse {

    private Integer score; // 0-100
    private List<HealthCheck> checks;
}

