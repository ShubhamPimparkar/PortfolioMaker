package com.developer.analytics.summary;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioAnalyticsSummaryRepository extends JpaRepository<PortfolioAnalyticsSummary, UUID> {

    Optional<PortfolioAnalyticsSummary> findByPortfolioUserId(UUID portfolioUserId);
}

