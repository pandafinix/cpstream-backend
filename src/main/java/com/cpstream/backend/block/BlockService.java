package com.cpstream.backend.block;

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

    public String blockUser(String viewerId, String targetUserId) {

        if (viewerId.equals(targetUserId)) {
            throw new RuntimeException("You cannot block yourself");
        }

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (blockRepository.existsByBlockerAndBlocked(viewer, target)) {
            throw new RuntimeException("Already blocked");
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

        return blockRepository.existsByBlockerAndBlocked(viewer, target);
    }

    public List<UserResponse> getBlockedUsers(String viewerId) {

        User viewer = userRepository.findById(viewerId)
                .orElseThrow(() -> new RuntimeException("Viewer not found"));

        return blockRepository.findByBlocker(viewer)
                .stream()
                .map(block -> mapToResponse(block.getBlocked()))
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