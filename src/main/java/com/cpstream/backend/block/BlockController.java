package com.cpstream.backend.block;

import com.cpstream.backend.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blocks")
@RequiredArgsConstructor
public class BlockController {

    private final BlockService blockService;

    @GetMapping
    public List<UserResponse> getBlockedUsers(@RequestParam String viewerId) {
        return blockService.getBlockedUsers(viewerId);
    }

    @PostMapping("/{targetUserId}")
    public String blockUser(
            @PathVariable String targetUserId,
            @RequestParam String viewerId
    ) {
        return blockService.blockUser(viewerId, targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    public String unblockUser(
            @PathVariable String targetUserId,
            @RequestParam String viewerId
    ) {
        return blockService.unblockUser(viewerId, targetUserId);
    }

    @GetMapping("/{targetUserId}/status")
    public boolean isBlocked(
            @PathVariable String targetUserId,
            @RequestParam String viewerId
    ) {
        return blockService.isBlocked(viewerId, targetUserId);
    }
}