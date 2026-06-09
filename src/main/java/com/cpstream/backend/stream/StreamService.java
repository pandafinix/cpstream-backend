package com.cpstream.backend.stream;

import com.cpstream.backend.block.Block;
import com.cpstream.backend.block.BlockRepository;
import com.cpstream.backend.user.User;
import com.cpstream.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository streamRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;

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

    public List<StreamResponse> getAllStreams(String viewerId) {
        return filterBlockedStreams(
                streamRepository.findAll(),
                viewerId
        )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StreamResponse> getLiveStreams(String viewerId) {
        return filterBlockedStreams(
                streamRepository.findByIsLiveTrue(),
                viewerId
        )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StreamResponse> searchStreams(
            String term,
            String viewerId
    ) {
        if (term == null || term.isBlank()) {
            return List.of();
        }

        return filterBlockedStreams(
                streamRepository.searchStreams(term.trim()),
                viewerId
        )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public StreamResponse updateStream(
            String streamId,
            StreamUpdateRequest request,
            String externalUserId
    ) {
        Stream stream = streamRepository.findById(streamId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stream not found"
                ));

        if (!externalUserId.equals(
                stream.getUser().getExternalUserId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this stream"
            );
        }

        if (request.getName() != null) {
            stream.setName(request.getName());
        }

        if (request.getThumbnailUrl() != null) {
            stream.setThumbnailUrl(request.getThumbnailUrl());
        }

        /*
         * isLive is intentionally not updated here.
         * Only the LiveKit webhook should change live status.
         */

        if (request.getIsChatEnabled() != null) {
            stream.setChatEnabled(request.getIsChatEnabled());
        }

        if (request.getIsChatDelayed() != null) {
            stream.setChatDelayed(request.getIsChatDelayed());
        }

        if (request.getIsChatFollowersOnly() != null) {
            stream.setChatFollowersOnly(
                    request.getIsChatFollowersOnly()
            );
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
        Stream stream = streamRepository
                .findByUserUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stream not found"
                ));

        return mapToResponse(stream);
    }

    @Transactional(readOnly = true)
    public StreamKeysResponse getStreamKeysByUsername(
            String username,
            String externalUserId
    ) {
        Stream stream = streamRepository
                .findByUserUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Stream not found"
                ));

        if (!externalUserId.equals(
                stream.getUser().getExternalUserId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this stream"
            );
        }

        return StreamKeysResponse.builder()
                .id(stream.getId())
                .ingressId(stream.getIngressId())
                .serverUrl(stream.getServerUrl())
                .streamKey(stream.getStreamKey())
                .username(stream.getUser().getUsername())
                .build();
    }

    private List<Stream> filterBlockedStreams(
            List<Stream> streams,
            String viewerId
    ) {
        if (viewerId == null || viewerId.isBlank()) {
            return streams;
        }

        User viewer = userRepository
                .findById(viewerId)
                .orElse(null);

        if (viewer == null) {
            return streams;
        }

        Set<String> blockedUserIds = new HashSet<>();

        List<Block> blockedByMe =
                blockRepository.findByBlocker(viewer);

        for (Block block : blockedByMe) {
            blockedUserIds.add(
                    block.getBlocked().getId()
            );
        }

        List<Block> blockedMe =
                blockRepository.findByBlocked(viewer);

        for (Block block : blockedMe) {
            blockedUserIds.add(
                    block.getBlocker().getId()
            );
        }

        return streams.stream()
                .filter(stream ->
                        !blockedUserIds.contains(
                                stream.getUser().getId()
                        )
                )
                .toList();
    }
}