package com.github.wsustudygroupapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Request body for POST /auth/register.
 * Sent by the frontend when a new student creates an account.
 */
@Data
@Schema(description = "Registration request — requires a valid Westfield State school email")
public class RegisterRequest {

    @Schema(description = "Must end with @westfield.ma.edu", example = "jjimenez@westfield.ma.edu")
    private String email;

    @Schema(description = "Plain text password — hashed with BCrypt before being stored", example = "myPassword123")
    private String password;

    @Schema(description = "Student's display name shown across the app", example = "Jose Jimenez")
    private String name;
}
