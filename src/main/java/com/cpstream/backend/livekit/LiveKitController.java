package com.cpstream.backend.livekit;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/livekit")
@RequiredArgsConstructor
public class LiveKitController {

    private final LiveKitService liveKitService;

    @PostMapping("/token")
    public LiveKitTokenResponse createToken(@RequestBody LiveKitTokenRequest request) {
        return liveKitService.createToken(request);
    }

    @PostMapping("/ingress")
    public LiveKitIngressResponse createIngress(@RequestBody LiveKitIngressRequest request) throws Exception {
        return liveKitService.createIngress(request);
    }
    @PostMapping("/webhook")
public String handleWebhook(@RequestBody LiveKitWebhookRequest request) {
    return liveKitService.handleWebhook(request);
}
}