package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Response body returned after a successful login.
 * The frontend stores the JWT token and sends it in the Authorization header on every subsequent request.
 */
@Data
@AllArgsConstructor
@Schema(description = "Authentication response containing the JWT token")
public class AuthResponse {

    @Schema(description = "JWT token — include in requests as: Authorization: Bearer <token>", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Human-readable result message", example = "Login successful")
    private String message;
}
