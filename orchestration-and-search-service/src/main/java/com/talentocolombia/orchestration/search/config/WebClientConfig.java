package com.talentocolombia.orchestration.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${candidate-profile-service.url}")
    private String candidateProfileServiceUrl;

    @Bean
    public WebClient candidateProfileWebClient() {
        return WebClient.builder()
                .baseUrl(candidateProfileServiceUrl)
                .build();
    }
}