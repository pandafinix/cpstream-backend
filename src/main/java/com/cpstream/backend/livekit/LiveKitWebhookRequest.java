package com.cpstream.backend.livekit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LiveKitWebhookRequest {

    private String event;

    private Room room;

    private IngressInfo ingressInfo;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Room {

        private String name;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IngressInfo {

        private String ingressId;

        private String roomName;
    }
}