package com.cpstream.backend.stream;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository streamRepository;
private StreamResponse mapToResponse(Stream stream) {
    return StreamResponse.builder()
            .id(stream.getId())
            .name(stream.getName())
            .thumbnailUrl(stream.getThumbnailUrl())
            .isLive(stream.isLive())
            .isChatEnabled(stream.isChatEnabled())
            .isChatDelayed(stream.isChatDelayed())
            .isChatFollowersOnly(stream.isChatFollowersOnly())
            .platform(stream.getPlatform())
            .difficulty(stream.getDifficulty())
            .language(stream.getLanguage())
            .username(stream.getUser().getUsername())
            .userImageUrl(stream.getUser().getImageUrl())
            .updatedAt(stream.getUpdatedAt())
            .build();
}
    public List<StreamResponse> getAllStreams() {
        return streamRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StreamResponse> getLiveStreams() {
        return streamRepository.findByIsLiveTrue()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StreamResponse> searchStreams(String term) {
        if (term == null || term.isBlank()) {
            return List.of();
        }

        return streamRepository.searchStreams(term.trim())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public StreamResponse updateStream(String streamId, StreamUpdateRequest request) {

        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        if (request.getName() != null) {
            stream.setName(request.getName());
        }

        if (request.getThumbnailUrl() != null) {
            stream.setThumbnailUrl(request.getThumbnailUrl());
        }

        if (request.getIsLive() != null) {
            stream.setLive(request.getIsLive());
        }

        if (request.getIsChatEnabled() != null) {
            stream.setChatEnabled(request.getIsChatEnabled());
        }

        if (request.getIsChatDelayed() != null) {
            stream.setChatDelayed(request.getIsChatDelayed());
        }

        if (request.getIsChatFollowersOnly() != null) {
            stream.setChatFollowersOnly(request.getIsChatFollowersOnly());
        }

        if (request.getPlatform() != null) {
            stream.setPlatform(request.getPlatform());
        }

        if (request.getDifficulty() != null) {
            stream.setDifficulty(request.getDifficulty());
        }

        if (request.getLanguage() != null) {
            stream.setLanguage(request.getLanguage());
        }

        stream = streamRepository.save(stream);

        return mapToResponse(stream);
    }

    public StreamResponse getStreamByUsername(String username) {

        Stream stream = streamRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Stream not found"));

        return mapToResponse(stream);
    }
}