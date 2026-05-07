package com.github.wsustudygroupapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BadgeResponseDTO {
    @JsonProperty("id")
    private String code;
    private String earnedAt;
}
