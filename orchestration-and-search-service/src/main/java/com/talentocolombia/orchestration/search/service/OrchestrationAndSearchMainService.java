package com.talentocolombia.orchestration.search.service;

import com.talentocolombia.jobs.scraper.grpc.JobOffersRequest;
import com.talentocolombia.jobs.scraper.grpc.JobOffersResponse;
import com.talentocolombia.orchestration.search.grpc.JobScraperGrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

// Ejemplo de cómo lo usarías en tu OrchestrationAndSearchService principal (aún por crear)
@Service
public class OrchestrationAndSearchMainService {

    private final JobScraperGrpcClient jobScraperGrpcClient;
    // ... otros clientes (Feign, etc.)

    @Autowired
    public OrchestrationAndSearchMainService(JobScraperGrpcClient jobScraperGrpcClient) {
        this.jobScraperGrpcClient = jobScraperGrpcClient;
    }

    public Mono<JobOffersResponse> searchJobOffers(String query) {
        JobOffersRequest request = JobOffersRequest.newBuilder()
                .setQuery(query)
                .build();
        // Llamada bloqueante, pero Spring WebFlux la envolverá en un Mono/Flux
        // Para llamadas reactivas puras, usarías el asyncStub y ReactorNetty.
        return Mono.fromCallable(() -> jobScraperGrpcClient.getJobOffers(request));
    }

    // ... otros métodos
}