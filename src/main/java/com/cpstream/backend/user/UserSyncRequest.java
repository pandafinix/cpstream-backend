package com.cpstream.backend.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSyncRequest {

    private String externalUserId;

    private String username;

    private String imageUrl;

    private String bio;
}