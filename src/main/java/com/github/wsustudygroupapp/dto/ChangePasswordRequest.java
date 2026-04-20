package com.github.wsustudygroupapp.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {

    private String token;
    private String newPassword;

}
