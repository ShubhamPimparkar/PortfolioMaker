package com.developer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.developer.entity.Achievement;
import com.developer.entity.User;

public interface AchievementRepository extends JpaRepository<Achievement, UUID> {

    @EntityGraph(attributePaths = {"user"})
    List<Achievement> findByUserOrderByIssueDateDescCreatedAtDesc(User user);
}

