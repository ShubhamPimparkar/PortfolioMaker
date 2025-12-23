package com.developer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.developer.entity.PortfolioSettings;
import com.developer.entity.User;

public interface PortfolioSettingsRepository extends JpaRepository<PortfolioSettings, UUID> {

    @EntityGraph(attributePaths = "user")
    Optional<PortfolioSettings> findByUser(User user);

    boolean existsByUser(User user);
}

