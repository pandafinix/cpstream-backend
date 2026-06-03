package com.cpstream.backend.user;

import com.cpstream.backend.stream.Stream;
import com.cpstream.backend.stream.StreamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.cpstream.backend.block.Block;
import com.cpstream.backend.block.BlockRepository;
import com.cpstream.backend.follow.Follow;
import com.cpstream.backend.follow.FollowRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final StreamRepository streamRepository;
    private final FollowRepository followRepository;
    private final BlockRepository blockRepository;

    public UserResponse syncUser(UserSyncRequest request) {

        if (request.getExternalUserId() == null || request.getExternalUserId().isBlank()) {
            throw new RuntimeException("External user id is required");
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new RuntimeException("Username is required");
        }

        User user = userRepository.findByExternalUserId(request.getExternalUserId())
                .orElse(null);

        if (user == null) {
            user = userRepository.findByUsername(request.getUsername())
                    .orElse(null);
        }

        if (user == null) {
            user = User.builder()
                    .externalUserId(request.getExternalUserId())
                    .username(request.getUsername())
                    .imageUrl(request.getImageUrl())
                    .bio(request.getBio())
                    .build();

            user = userRepository.save(user);

            Stream stream = Stream.builder()
                    .name(request.getUsername() + "'s stream")
                    .user(user)
                    .build();

            streamRepository.save(stream);
        } else {
            user.setExternalUserId(request.getExternalUserId());
            user.setImageUrl(request.getImageUrl());

            if (request.getBio() != null) {
                user.setBio(request.getBio());
            }

            user = userRepository.save(user);
        }

        return mapToResponse(user);
    }

    public UserResponse getUserByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    public List<UserResponse> getRecommendedUsers(String viewerId) {

        List<User> users = userRepository.findAll();

        if (viewerId == null || viewerId.isBlank()) {
            return users.stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        Set<String> remove = new HashSet<>();

        remove.add(viewer.getId());

        List<Follow> following = followRepository.findByFollower(viewer);
        for (Follow it : following) {
            remove.add(it.getFollowing().getId());
        }

        List<Block> blockedByMe = blockRepository.findByBlocker(viewer);
        for (Block it : blockedByMe) {
            remove.add(it.getBlocked().getId());
        }

        List<Block> blockedMe = blockRepository.findByBlocked(viewer);
        for (Block it : blockedMe) {
            remove.add(it.getBlocker().getId());
        }

        return users.stream()
                .filter(user -> !remove.contains(user.getId()))
                .map(this::mapToResponse)
                .toList();
    }
public UserResponse updateUsername(String userId, UserUpdateUsernameRequest request) {

    if (request.getUsername() == null || request.getUsername().isBlank()) {
        throw new RuntimeException("Username is required");
    }

    String newUsername = request.getUsername().trim();

    if (!newUsername.matches("^[a-zA-Z0-9_]{3,30}$")) {
        throw new RuntimeException("Username can only contain letters, numbers, and underscores");
    }

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    if (!user.getUsername().equals(newUsername)
            && userRepository.existsByUsername(newUsername)) {
        throw new RuntimeException("Username already taken");
    }

    String oldUsername = user.getUsername();

    user.setUsername(newUsername);
    user = userRepository.save(user);

    Stream stream = streamRepository.findByUserUsername(newUsername).orElse(null);

    if (stream != null
            && stream.getName() != null
            && stream.getName().equals(oldUsername + "'s stream")) {
        stream.setName(newUsername + "'s stream");
        streamRepository.save(stream);
    }

    return mapToResponse(user);
}
public UserResponse updateBio(String userId, UserUpdateBioRequest request) {

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String bio = request.getBio();

    if (bio != null) {
        bio = bio.trim();
    }

    if (bio != null && bio.length() > 500) {
        throw new RuntimeException("Bio must be at most 500 characters");
    }

    user.setBio(bio == null ? "" : bio);

    user = userRepository.save(user);

    return mapToResponse(user);
}
public List<UserResponse> searchUsers(String term) {

    if (term == null || term.isBlank()) {
        return List.of();
    }

    return userRepository.findByUsernameContainingIgnoreCase(term.trim())
            .stream()
            .map(this::mapToResponse)
            .toList();
}
    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .externalUserId(user.getExternalUserId())
                .username(user.getUsername())
                .imageUrl(user.getImageUrl())
                .bio(user.getBio())
                .build();
    }
}