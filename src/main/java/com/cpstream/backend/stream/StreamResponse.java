package com.cpstream.backend.stream;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class StreamResponse {

    private String id;

    private String name;

    private String thumbnailUrl;

    private boolean isLive;

    private boolean isChatEnabled;

    private boolean isChatDelayed;

    private boolean isChatFollowersOnly;

    private String platform;

    private String difficulty;

    private String language;

    private String username;

    private String userImageUrl;

    private LocalDateTime updatedAt;
}