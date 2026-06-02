package com.cpstream.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByExternalUserId(String externalUserId);

    boolean existsByUsername(String username);

    boolean existsByExternalUserId(String externalUserId);

    List<User> findByUsernameContainingIgnoreCase(String term);
}