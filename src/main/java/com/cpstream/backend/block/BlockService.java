package com.cpstream.backend.block;

import com.cpstream.backend.follow.Follow;
import com.cpstream.backend.follow.FollowRepository;
import com.cpstream.backend.user.User;
import com.cpstream.backend.user.UserRepository;
import com.cpstream.backend.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockService {

    private final BlockRepository blockRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public String blockUser(String viewerId, String targetUserId) {

        if (viewerId.equals(targetUserId)) {
            throw new RuntimeException("You cannot block yourself");
        }

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        removeFollowIfExists(viewer, target);
        removeFollowIfExists(target, viewer);

        if (blockRepository.existsByBlockerAndBlocked(viewer, target)) {
            return "Blocked successfully";
        }

        Block block = Block.builder()
                .blocker(viewer)
                .blocked(target)
                .build();

        blockRepository.save(block);

        return "Blocked successfully";
    }

    public String unblockUser(String viewerId, String targetUserId) {

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Block block = blockRepository.findByBlockerAndBlocked(viewer, target)
                .orElseThrow(() -> new RuntimeException("Block not found"));

        blockRepository.delete(block);

        return "Unblocked successfully";
    }

    public boolean isBlocked(String viewerId, String targetUserId) {

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        return blockRepository.existsByBlockerAndBlocked(viewer, target)
                || blockRepository.existsByBlockerAndBlocked(target, viewer);
    }

    public List<UserResponse> getBlockedUsers(String viewerId) {

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        return blockRepository.findByBlocker(viewer)
                .stream()
                .map(block -> mapToResponse(block.getBlocked()))
                .toList();
    }

    private void removeFollowIfExists(User follower, User following) {
        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElse(null);

        if (follow != null) {
            followRepository.delete(follow);
        }
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