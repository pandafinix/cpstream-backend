package com.cpstream.backend.follow;

import com.cpstream.backend.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @GetMapping
    public List<UserResponse> getFollowingUsers(@RequestParam String viewerId) {
        return followService.getFollowingUsers(viewerId);
    }

    @PostMapping("/{targetUserId}")
    public String followUser(
            @PathVariable String targetUserId,
            @RequestParam String viewerId
    ) {
        return followService.followUser(viewerId, targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    public String unfollowUser(
            @PathVariable String targetUserId,
            @RequestParam String viewerId
    ) {
        return followService.unfollowUser(viewerId, targetUserId);
    }

    @GetMapping("/{targetUserId}/count")
    public long getFollowerCount(@PathVariable String targetUserId) {
        return followService.getFollowerCount(targetUserId);
    }

    @GetMapping("/{targetUserId}/status")
    public boolean isFollowing(
            @PathVariable String targetUserId,
            @RequestParam String viewerId
    ) {
        return followService.isFollowing(viewerId, targetUserId);
    }
}