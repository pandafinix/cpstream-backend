package com.cpstream.backend.livekit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livekit")
@RequiredArgsConstructor
public class LiveKitController {

    private final LiveKitService liveKitService;

    @PostMapping("/token")
    public LiveKitTokenResponse createToken(
            @RequestBody LiveKitTokenRequest request
    ) {
        return liveKitService.createToken(request);
    }

    @PostMapping("/ingress")
    public LiveKitIngressResponse createIngress(
            @RequestBody LiveKitIngressRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) throws Exception {
        return liveKitService.createIngress(
                request,
                jwt.getSubject()
        );
    }

    @PostMapping("/webhook")
    public String handleWebhook(
            @RequestBody LiveKitWebhookRequest request
    ) {
        return liveKitService.handleWebhook(request);
    }

    @DeleteMapping("/ingress/{ingressId}")
    public String deleteIngress(
            @PathVariable String ingressId,
            @AuthenticationPrincipal Jwt jwt
    ) throws Exception {
        return liveKitService.deleteIngress(
                ingressId,
                jwt.getSubject()
        );
    }
}