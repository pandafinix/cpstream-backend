package com.cpstream.backend.livekit;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LiveKitTokenRequest {

    private String roomName;

    private String identity;

    private String name;

    private boolean canPublish;
}