package com.developer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.developer.entity.Project;
import com.developer.entity.User;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @EntityGraph(attributePaths = {"user","techStack"})
    List<Project> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {"user","techStack"})
    List<Project> findByUserAndIsPublicTrueOrderByCreatedAtDesc(User user);
}


