package com.talentocolombia.orchestration.search.service;

import com.talentocolombia.orchestration.search.dto.CandidateProfileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class CandidateProfileClientService {

    private final WebClient webClient;

    @Autowired
    public CandidateProfileClientService(WebClient candidateProfileWebClient) {
        this.webClient = candidateProfileWebClient;
    }

    public Mono<CandidateProfileDto> getCandidateProfileById(UUID id) {
        return webClient.get()
                .uri("/api/v1/candidates/{id}", id)
                .retrieve()
                .bodyToMono(CandidateProfileDto.class);
    }

    public Mono<List<CandidateProfileDto>> getAllCandidateProfiles() {
        return webClient.get()
                .uri("/api/v1/candidates")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<CandidateProfileDto>>() {});
    }
}