package com.cpstream.backend.livekit;

import com.cpstream.backend.stream.Stream;
import com.cpstream.backend.stream.StreamRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.livekit.server.IngressServiceClient;
import livekit.LivekitIngress;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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

    public LiveKitTokenResponse createToken(
            LiveKitTokenRequest request
    ) {

        SecretKey key = Keys.hmacShaKeyFor(
                apiSecret.getBytes(StandardCharsets.UTF_8)
        );

        Map<String, Object> videoGrant = new HashMap<>();

        videoGrant.put("roomJoin", true);
        videoGrant.put("room", request.getRoomName());
        videoGrant.put("canSubscribe", true);
        videoGrant.put(
                "canPublish",
                request.isCanPublish()
        );
        videoGrant.put("canPublishData", true);

        Instant now = Instant.now();

        String token = Jwts.builder()
                .issuer(apiKey)
                .subject(request.getIdentity())
                .id(UUID.randomUUID().toString())
                .claim("name", request.getName())
                .claim("video", videoGrant)
                .issuedAt(Date.from(now))
                .expiration(
                        Date.from(
                                now.plusSeconds(60 * 60 * 6)
                        )
                )
                .signWith(key)
                .compact();

        return LiveKitTokenResponse.builder()
                .token(token)
                .serverUrl(wsUrl)
                .build();
    }

    @Transactional
    public String handleWebhook(
            LiveKitWebhookRequest request
    ) {

        if (request == null) {
            return "Webhook ignored: empty request";
        }

        String event = request.getEvent();
        Stream stream = null;

        /*
         * Prefer ingressId because it remains stable.
         */
        if (request.getIngressInfo() != null) {

            String ingressId =
                    request.getIngressInfo()
                            .getIngressId();

            if (ingressId != null
                    && !ingressId.isBlank()) {

                stream = streamRepository
                        .findByIngressId(ingressId)
                        .orElse(null);
            }
        }

        /*
         * Fallback to room name for room events
         * or test events without an ingressId.
         */
        if (stream == null) {

            String roomName = null;

            if (request.getIngressInfo() != null) {
                roomName = request
                        .getIngressInfo()
                        .getRoomName();
            }

            if ((roomName == null
                    || roomName.isBlank())
                    && request.getRoom() != null) {

                roomName = request
                        .getRoom()
                        .getName();
            }

            if (roomName != null
                    && !roomName.isBlank()) {

                stream = streamRepository
                        .findByUserUsername(roomName)
                        .orElse(null);
            }
        }

        /*
         * LiveKit test events may use a dummy room.
         * Return normally so the endpoint responds 200.
         */
        if (stream == null) {
            return "Webhook ignored: no matching stream";
        }

        if ("ingress_started".equals(event)) {

            stream.setLive(true);
            streamRepository.save(stream);

            return "Stream marked live";
        }

        if ("ingress_ended".equals(event)
                || "room_finished".equals(event)) {

            stream.setLive(false);
            streamRepository.save(stream);

            return "Stream marked offline";
        }

        return "Webhook received but no action taken";
    }

    @Transactional
    public LiveKitIngressResponse createIngress(
            LiveKitIngressRequest request,
            String externalUserId
    ) throws Exception {

        if (request.getUsername() == null
                || request.getUsername().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username is required"
            );
        }

        Stream stream = streamRepository
                .findByUserUsername(
                        request.getUsername()
                )
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Stream not found"
                        )
                );

        if (!externalUserId.equals(
                stream.getUser()
                        .getExternalUserId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this stream"
            );
        }

        boolean connectionAlreadyExists =
                stream.getIngressId() != null
                        && !stream.getIngressId()
                        .isBlank()
                        && stream.getServerUrl() != null
                        && !stream.getServerUrl()
                        .isBlank()
                        && stream.getStreamKey() != null
                        && !stream.getStreamKey()
                        .isBlank();

        if (connectionAlreadyExists) {

            return LiveKitIngressResponse.builder()
                    .ingressId(stream.getIngressId())
                    .serverUrl(stream.getServerUrl())
                    .streamKey(stream.getStreamKey())
                    .roomName(request.getUsername())
                    .build();
        }

        String roomName = request.getUsername();

        String participantIdentity =
                request.getUsername() + "-host";

        String participantName =
                request.getUsername();

        String apiUrl = wsUrl
                .replace("wss://", "https://")
                .replace("ws://", "http://");

        IngressServiceClient ingressClient =
                IngressServiceClient.create(
                        apiUrl,
                        apiKey,
                        apiSecret
                );

        Response<LivekitIngress.IngressInfo> response =
                ingressClient.createIngress(
                        request.getUsername()
                                + " ingress",
                        roomName,
                        participantIdentity,
                        participantName,
                        LivekitIngress.IngressInput
                                .RTMP_INPUT
                ).execute();

        if (!response.isSuccessful()
                || response.body() == null) {

            String errorBody = "";

            if (response.errorBody() != null) {
                errorBody = response
                        .errorBody()
                        .string();
            }

            throw new RuntimeException(
                    "Failed to create ingress. "
                            + "LiveKit status: "
                            + response.code()
                            + " "
                            + response.message()
                            + " "
                            + errorBody
            );
        }

        LivekitIngress.IngressInfo ingress =
                response.body();

        stream.setIngressId(
                ingress.getIngressId()
        );
        stream.setServerUrl(
                ingress.getUrl()
        );
        stream.setStreamKey(
                ingress.getStreamKey()
        );
        stream.setLive(false);

        streamRepository.save(stream);

        return LiveKitIngressResponse.builder()
                .ingressId(
                        ingress.getIngressId()
                )
                .serverUrl(
                        ingress.getUrl()
                )
                .streamKey(
                        ingress.getStreamKey()
                )
                .roomName(roomName)
                .build();
    }

    @Transactional
    public String deleteIngress(
            String ingressId,
            String externalUserId
    ) throws Exception {

        if (ingressId == null
                || ingressId.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ingress id is required"
            );
        }

        Stream stream = streamRepository
                .findByIngressId(ingressId)
                .orElseThrow(
                        () -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ingress not found"
                        )
                );

        if (!externalUserId.equals(
                stream.getUser()
                        .getExternalUserId()
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this ingress"
            );
        }

        String apiUrl = wsUrl
                .replace("wss://", "https://")
                .replace("ws://", "http://");

        IngressServiceClient ingressClient =
                IngressServiceClient.create(
                        apiUrl,
                        apiKey,
                        apiSecret
                );

        Response<?> response =
                ingressClient
                        .deleteIngress(ingressId)
                        .execute();

        if (!response.isSuccessful()) {

            String errorBody = "";

            if (response.errorBody() != null) {
                errorBody = response
                        .errorBody()
                        .string();
            }

            throw new RuntimeException(
                    "Failed to delete ingress. "
                            + "LiveKit status: "
                            + response.code()
                            + " "
                            + response.message()
                            + " "
                            + errorBody
            );
        }

        stream.setIngressId(null);
        stream.setServerUrl(null);
        stream.setStreamKey(null);
        stream.setLive(false);

        streamRepository.save(stream);

        return "Ingress deleted successfully";
    }
}