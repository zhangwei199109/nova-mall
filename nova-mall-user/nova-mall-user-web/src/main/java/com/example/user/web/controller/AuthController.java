package com.example.user.web.controller;

import com.example.common.dto.Result;
import com.example.user.web.dto.LoginRequest;
import com.example.user.web.dto.LoginResponse;
import com.example.user.web.dto.RefreshRequest;
import com.example.user.web.service.TokenService;
import org.apache.commons.lang3.StringUtils;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    // 简单内存用户表：用户名 -> [密码, 角色...]
    private final Map<String, UserRecord> users;

    public AuthController(TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.users = Map.of(
                "admin", new UserRecord(passwordEncoder.encode("admin123"), List.of("ROLE_ADMIN", "ROLE_USER")),
                "user", new UserRecord(passwordEncoder.encode("user123"), List.of("ROLE_USER"))
        );
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        UserRecord record = users.get(StringUtils.trim(req.getUsername()));
        if (record == null || !passwordEncoder.matches(req.getPassword(), record.password())) {
            return Result.error(401, "用户名或密码错误");
        }
        String userId = "U-" + req.getUsername();
        TokenService.AuthTokens tokens = tokenService.generateTokens(userId, req.getUsername(), record.roles());
        return Result.success(new LoginResponse(tokens.accessToken(), tokens.refreshToken(), tokens.userId(), tokens.username(), tokens.roles()));
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        TokenService.AuthTokens tokens = tokenService.refresh(req.getRefreshToken());
        return Result.success(new LoginResponse(tokens.accessToken(), tokens.refreshToken(), tokens.userId(), tokens.username(), tokens.roles()));
    }

    private record UserRecord(String password, List<String> roles) {}
}

