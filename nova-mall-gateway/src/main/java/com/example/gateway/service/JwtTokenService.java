package com.example.gateway.service;

import com.example.gateway.config.JwtAuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenService {

    private final SecretKey secretKey;
    private final JwtAuthProperties props;

    public JwtTokenService(JwtAuthProperties props) {
        this.props = props;
        this.secretKey = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(props.getLeeway().toSeconds())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}





























