package com.developer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.developer.entity.Profile;
import com.developer.entity.User;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    @EntityGraph(attributePaths = {"skills", "user"})
    Optional<Profile> findByUser(User user);

    boolean existsByUser(User user);
}


