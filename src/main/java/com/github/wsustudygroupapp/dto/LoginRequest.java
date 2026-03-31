package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Request body for POST /auth/login.
 * Sent by the frontend when a student signs in.
 */
@Data
@Schema(description = "Login request — account must be verified before login is allowed")
public class LoginRequest {

    @Schema(description = "Registered school email", example = "jjimenez@westfield.ma.edu")
    private String email;

    @Schema(description = "Account password", example = "myPassword123")
    private String password;
}
