package com.cpstream.backend.follow;

import com.cpstream.backend.block.BlockRepository;
import com.cpstream.backend.user.User;
import com.cpstream.backend.user.UserRepository;
import com.cpstream.backend.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final BlockRepository blockRepository;

    public String followUser(String viewerId, String targetUserId) {

        if (viewerId.equals(targetUserId)) {
            throw new RuntimeException("You cannot follow yourself");
        }

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (isBlockedEitherWay(viewer, target)) {
            throw new RuntimeException("Cannot follow this user");
        }

        if (followRepository.existsByFollowerAndFollowing(viewer, target)) {
            throw new RuntimeException("Already following");
        }

        Follow follow = Follow.builder()
                .follower(viewer)
                .following(target)
                .build();

        followRepository.save(follow);

        return "Followed successfully";
    }

    public String unfollowUser(String viewerId, String targetUserId) {

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Follow follow = followRepository.findByFollowerAndFollowing(viewer, target)
                .orElseThrow(() -> new RuntimeException("Follow not found"));

        followRepository.delete(follow);

        return "Unfollowed successfully";
    }

    public long getFollowerCount(String targetUserId) {

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        return followRepository.countByFollowing(target);
    }

    public boolean isFollowing(String viewerId, String targetUserId) {

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (isBlockedEitherWay(viewer, target)) {
            return false;
        }

        return followRepository.existsByFollowerAndFollowing(viewer, target);
    }

    public List<UserResponse> getFollowingUsers(String viewerId) {

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        return followRepository.findByFollower(viewer)
                .stream()
                .map(Follow::getFollowing)
                .filter(user -> !isBlockedEitherWay(viewer, user))
                .map(this::mapToResponse)
                .toList();
    }

    private boolean isBlockedEitherWay(User viewer, User target) {
        return blockRepository.existsByBlockerAndBlocked(viewer, target)
                || blockRepository.existsByBlockerAndBlocked(target, viewer);
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