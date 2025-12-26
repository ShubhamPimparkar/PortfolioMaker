package com.developer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.developer.entity.Education;
import com.developer.entity.User;

public interface EducationRepository extends JpaRepository<Education, UUID> {

    @EntityGraph(attributePaths = {"user"})
    List<Education> findByUserOrderByStartDateDesc(User user);
}

