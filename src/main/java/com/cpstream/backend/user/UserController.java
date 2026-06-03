package com.cpstream.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/sync")
    public UserResponse syncUser(@RequestBody UserSyncRequest request) {
        return userService.syncUser(request);
    }
    @PatchMapping("/{userId}/bio")
public UserResponse updateBio(
        @PathVariable String userId,
        @RequestBody UserUpdateBioRequest request
) {
    return userService.updateBio(userId, request);
}

    @GetMapping("/recommended")
    public List<UserResponse> getRecommendedUsers(
            @RequestParam(required = false) String viewerId
    ) {
        return userService.getRecommendedUsers(viewerId);
    }

    @GetMapping("/search")
    public List<UserResponse> searchUsers(@RequestParam String term) {
        return userService.searchUsers(term);
    }

    @GetMapping("/{username}")
    public UserResponse getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username);
    }

    @PatchMapping("/{userId}/username")
    public UserResponse updateUsername(
            @PathVariable String userId,
            @RequestBody UserUpdateUsernameRequest request
    ) {
        return userService.updateUsername(userId, request);
    }
}