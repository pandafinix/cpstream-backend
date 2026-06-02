package com.cpstream.backend.block;

import com.cpstream.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, String> {

    Optional<Block> findByBlockerAndBlocked(User blocker, User blocked);

    boolean existsByBlockerAndBlocked(User blocker, User blocked);

    List<Block> findByBlocker(User blocker);

    List<Block> findByBlocked(User blocked);
}