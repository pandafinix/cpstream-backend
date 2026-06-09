package com.cpstream.backend.stream;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StreamUpdateRequest {

    private String name;

    private String thumbnailUrl;

    private Boolean isChatEnabled;

    private Boolean isChatDelayed;

    private Boolean isChatFollowersOnly;

    private String platform;

    private String difficulty;

    private String language;
}