package com.github.wsustudygroupapp.dto;

import lombok.Data;

/** Request body for POST /groups/{groupId}/join. */
@Data
public class JoinGroupRequest {
    private String password;
}
