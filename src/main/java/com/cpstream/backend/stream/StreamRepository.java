package com.cpstream.backend.stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, String> {

    List<Stream> findByIsLiveTrue();

    List<Stream> findByNameContainingIgnoreCase(String term);

    Optional<Stream> findByUserUsername(String username);

    Optional<Stream> findByIngressId(String ingressId);

    @Query("""
            SELECT s FROM Stream s
            JOIN s.user u
            WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(s.platform) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(s.difficulty) LIKE LOWER(CONCAT('%', :term, '%'))
               OR LOWER(s.language) LIKE LOWER(CONCAT('%', :term, '%'))
            """)
    List<Stream> searchStreams(@Param("term") String term);
}