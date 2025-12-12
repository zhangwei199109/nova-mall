package com.example.user.web.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String username;
    private List<String> roles;

    public LoginResponse(String token, String refreshToken, String userId, String username, List<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }
}

