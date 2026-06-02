package com.cpstream.backend.livekit;

import com.cpstream.backend.stream.Stream;
import com.cpstream.backend.stream.StreamRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.livekit.server.IngressServiceClient;
import livekit.LivekitIngress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Response;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiveKitService {

    private final StreamRepository streamRepository;

    @Value("${livekit.api-key}")
    private String apiKey;

    @Value("${livekit.api-secret}")
    private String apiSecret;

    @Value("${livekit.ws-url}")
    private String wsUrl;

    public LiveKitTokenResponse createToken(LiveKitTokenRequest request) {

        SecretKey key = Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> videoGrant = new HashMap<>();
        videoGrant.put("roomJoin", true);
        videoGrant.put("room", request.getRoomName());
        videoGrant.put("canSubscribe", true);
        videoGrant.put("canPublish", request.isCanPublish());
        videoGrant.put("canPublishData", true);

        Instant now = Instant.now();

        String token = Jwts.builder()
                .issuer(apiKey)
                .subject(request.getIdentity())
                .id(UUID.randomUUID().toString())
                .claim("name", request.getName())
                .claim("video", videoGrant)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(60 * 60 * 6)))
                .signWith(key)
                .compact();

        return LiveKitTokenResponse.builder()
                .token(token)
                .serverUrl(wsUrl)
                .build();
    }

    public LiveKitIngressResponse createIngress(LiveKitIngressRequest request) throws Exception {

    Stream stream = streamRepository.findByUserUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Stream not found"));

    String roomName = request.getUsername();
    String participantIdentity = request.getUsername() + "-host";
    String participantName = request.getUsername();

    String apiUrl = wsUrl
            .replace("wss://", "https://")
            .replace("ws://", "http://");

    IngressServiceClient ingressClient = IngressServiceClient.create(
            apiUrl,
            apiKey,
            apiSecret
    );

    if (stream.getIngressId() != null && !stream.getIngressId().isBlank()) {
        try {
            ingressClient.deleteIngress(stream.getIngressId()).execute();
        } catch (Exception ignored) {
            // old ingress may already be deleted or inactive
        }
    }

    Response<LivekitIngress.IngressInfo> response = ingressClient.createIngress(
            request.getUsername() + " ingress",
            roomName,
            participantIdentity,
            participantName,
            LivekitIngress.IngressInput.RTMP_INPUT
    ).execute();

    if (!response.isSuccessful() || response.body() == null) {
        throw new RuntimeException("Failed to create ingress");
    }

    LivekitIngress.IngressInfo ingress = response.body();

    stream.setIngressId(ingress.getIngressId());
    stream.setServerUrl(ingress.getUrl());
    stream.setStreamKey(ingress.getStreamKey());
    stream.setLive(false);

    streamRepository.save(stream);

    return LiveKitIngressResponse.builder()
            .ingressId(ingress.getIngressId())
            .serverUrl(ingress.getUrl())
            .streamKey(ingress.getStreamKey())
            .roomName(roomName)
            .build();
}

    public String handleWebhook(LiveKitWebhookRequest request) {

    String event = request.getEvent();

    String roomName = null;

    if (request.getIngressInfo() != null && request.getIngressInfo().getRoomName() != null) {
        roomName = request.getIngressInfo().getRoomName();
    } else if (request.getRoom() != null && request.getRoom().getName() != null) {
        roomName = request.getRoom().getName();
    }

    if (roomName == null) {
        return "Webhook received but room name not found";
    }

    Stream stream = streamRepository.findByUserUsername(roomName)
            .orElseThrow(() -> new RuntimeException("Stream not found"));

    if ("ingress_started".equals(event)) {
        stream.setLive(true);
        streamRepository.save(stream);
        return "Stream marked live";
    }

    if ("ingress_ended".equals(event)) {
        stream.setLive(false);
        streamRepository.save(stream);
        return "Stream marked offline";
    }

    if ("room_finished".equals(event)) {
        stream.setLive(false);
        streamRepository.save(stream);
        return "Room finished, stream marked offline";
    }

    return "Webhook received but no action taken";
}
}