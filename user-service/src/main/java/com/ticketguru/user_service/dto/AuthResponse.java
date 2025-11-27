package com.ticketguru.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private UserDto user;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long accessTokenExpiresAt;
}