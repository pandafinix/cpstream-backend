package com.cpstream.backend.livekit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LiveKitIngressResponse {

    private String ingressId;

    private String serverUrl;

    private String streamKey;

    private String roomName;
}