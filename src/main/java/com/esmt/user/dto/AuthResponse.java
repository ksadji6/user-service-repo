package com.esmt.user.dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AuthResponse {
    private String token;
    private String type;
    private Long userId;
    private String email;
    private String role;
    private UserResponse user;
}