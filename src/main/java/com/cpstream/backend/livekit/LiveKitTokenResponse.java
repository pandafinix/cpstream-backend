package com.cpstream.backend.livekit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LiveKitTokenResponse {

    private String token;

    private String serverUrl;
}