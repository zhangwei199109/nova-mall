package com.example.user.web.controller;

import com.example.common.dto.Result;
import com.example.user.api.dto.UserDTO;
import com.example.user.service.UserAppService;
import com.example.user.web.dto.*;
import com.example.user.web.service.NotificationService;
import com.example.user.web.service.RateLimiterService;
import com.example.user.web.service.CaptchaService;
import com.example.user.web.service.TokenService;
import org.apache.commons.lang3.StringUtils;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UserAppService userAppService;
    private final CaptchaService captchaService;
    private final NotificationService notificationService;
    private final RateLimiterService rateLimiterService;

    public AuthController(TokenService tokenService, PasswordEncoder passwordEncoder,
                          UserAppService userAppService, CaptchaService captchaService,
                          NotificationService notificationService, RateLimiterService rateLimiterService) {
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.userAppService = userAppService;
        this.captchaService = captchaService;
        this.notificationService = notificationService;
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req, jakarta.servlet.http.HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        if (!rateLimiterService.allowIp(ip)) {
            return Result.error(429, "请求过于频繁");
        }
        if (!rateLimiterService.allowAccount(req.getUsername())) {
            return Result.error(429, "账户尝试过多，请稍后再试");
        }
        String identifier = StringUtils.trim(req.getUsername());
        UserDTO user = userAppService.findByIdentifier(identifier);
        if (user == null) {
            return Result.error(401, "账号或密码错误");
        }
        if (StringUtils.isBlank(user.getPassword()) || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return Result.error(401, "账号或密码错误");
        }
        String deviceId = StringUtils.defaultIfBlank(req.getDeviceId(), "dev-" + java.util.UUID.randomUUID());
        List<String> roles = List.of("ROLE_USER");
        TokenService.AuthTokens tokens = tokenService.generateTokens(
                String.valueOf(user.getId()), user.getUsername(), roles, deviceId, ip, httpRequest.getHeader("User-Agent"));
        return Result.success(new LoginResponse(tokens.accessToken(), tokens.refreshToken(), tokens.userId(), tokens.username(), tokens.roles()));
    }

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest req) {
        // 简单防刷：依赖后续风控/验证码
        String encoded = passwordEncoder.encode(req.getPassword());
        UserDTO created = userAppService.register(req.getUsername(), req.getEmail(), req.getMobile(), encoded);
        return Result.success(created);
    }

    @PostMapping("/refresh")
    public Result<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        TokenService.AuthTokens tokens = tokenService.refresh(req.getRefreshToken());
        return Result.success(new LoginResponse(tokens.accessToken(), tokens.refreshToken(), tokens.userId(), tokens.username(), tokens.roles()));
    }

    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@Valid @RequestBody SendResetCodeRequest req) {
        String key = normalizeAccount(req.getAccount());
        String code = captchaService.generateCode(key);
        if (key.contains("@")) {
            notificationService.sendMail(key, "Password Reset Code", "Your code is: " + code);
        } else {
            notificationService.sendSms(key, "RESET_PWD", code);
        }
        // dev 环境可直接返回，生产可配置关闭
        return Result.success(code);
    }

    @PostMapping("/reset-password")
    public Result<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        String key = normalizeAccount(req.getAccount());
        boolean ok = captchaService.verify(key, req.getCode());
        if (!ok) {
            return Result.error(400, "验证码错误或已过期");
        }
        UserDTO user = userAppService.findByIdentifier(key);
        if (user == null) {
            return Result.error(404, "账号不存在");
        }
        String encoded = passwordEncoder.encode(req.getNewPassword());
        boolean updated = userAppService.updatePassword(user.getId(), encoded);
        return updated ? Result.success(true) : Result.error(500, "重置失败");
    }

    private String normalizeAccount(String account) {
        return StringUtils.trim(account);
    }

    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip)) {
            return ip.split(",")[0].trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(ip)) {
            return ip;
        }
        return request.getRemoteAddr();
    }
}

