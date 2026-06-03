package com.cpstream.backend.stream;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StreamKeysResponse {

    private String id;

    private String ingressId;

    private String serverUrl;

    private String streamKey;

    private String username;
}