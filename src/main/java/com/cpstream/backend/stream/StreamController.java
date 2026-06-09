package com.cpstream.backend.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @GetMapping
    public List<StreamResponse> getAllStreams(
            @RequestParam(required = false) String viewerId
    ) {
        return streamService.getAllStreams(viewerId);
    }

    @GetMapping("/live")
    public List<StreamResponse> getLiveStreams(
            @RequestParam(required = false) String viewerId
    ) {
        return streamService.getLiveStreams(viewerId);
    }

    @GetMapping("/search")
    public List<StreamResponse> searchStreams(
            @RequestParam String term,
            @RequestParam(required = false) String viewerId
    ) {
        return streamService.searchStreams(term, viewerId);
    }

    @PatchMapping("/{streamId}")
    public StreamResponse updateStream(
            @PathVariable String streamId,
            @RequestBody StreamUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return streamService.updateStream(
                streamId,
                request,
                jwt.getSubject()
        );
    }

    @GetMapping("/user/{username}")
    public StreamResponse getStreamByUsername(
            @PathVariable String username
    ) {
        return streamService.getStreamByUsername(username);
    }

    @GetMapping("/user/{username}/keys")
    public StreamKeysResponse getStreamKeysByUsername(
            @PathVariable String username,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return streamService.getStreamKeysByUsername(
                username,
                jwt.getSubject()
        );
    }
}