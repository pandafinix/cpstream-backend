package com.cpstream.backend.stream;

import com.cpstream.backend.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "streams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(unique = true)
    private String ingressId;

    @Column(columnDefinition = "TEXT")
    private String serverUrl;

    @Column(columnDefinition = "TEXT")
    private String streamKey;

    @Builder.Default
    private boolean isLive = false;

    @Builder.Default
    private boolean isChatEnabled = true;

    @Builder.Default
    private boolean isChatDelayed = false;

    @Builder.Default
    private boolean isChatFollowersOnly = false;

    private String platform;

    private String difficulty;

    private String language;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}