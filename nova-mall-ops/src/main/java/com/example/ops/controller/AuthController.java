package com.example.ops.controller;

import com.example.common.dto.Result;
import com.example.ops.config.OpsAuthProperties;
import com.example.ops.util.JwtUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final OpsAuthProperties authProperties;

    @PostMapping("/login")
    public Result<String> login(@RequestBody @Validated LoginRequest req) {
        if (!authProperties.getUsername().equals(req.getUsername())
                || !authProperties.getPassword().equals(req.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }
        String token = JwtUtil.generate(req.getUsername(),
                List.of("ROLE_OPS_ADMIN"),
                authProperties.getJwtSecret(),
                authProperties.getExpireMinutes());
        return Result.success(token);
    }

    @Data
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }
}

















