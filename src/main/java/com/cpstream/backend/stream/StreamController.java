package com.cpstream.backend.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @GetMapping
    public List<StreamResponse> getAllStreams() {
        return streamService.getAllStreams();
    }

    @GetMapping("/live")
    public List<StreamResponse> getLiveStreams() {
        return streamService.getLiveStreams();
    }

    @GetMapping("/search")
    public List<StreamResponse> searchStreams(@RequestParam String term) {
        return streamService.searchStreams(term);
    }
    @PatchMapping("/{streamId}")
public StreamResponse updateStream(
        @PathVariable String streamId,
        @RequestBody StreamUpdateRequest request
) {
    return streamService.updateStream(streamId, request);
}
@GetMapping("/user/{username}")
public StreamResponse getStreamByUsername(@PathVariable String username) {
    return streamService.getStreamByUsername(username);
}
}