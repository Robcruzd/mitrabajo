package com.talentocolombia.gateway.job.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withSecretKey(secretKey())
                .build();
    }

    @Bean
    public SecretKey secretKey() {
        return new SecretKeySpec(
                "super-secret-key-super-secret-key"
                        .getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
    }
}

