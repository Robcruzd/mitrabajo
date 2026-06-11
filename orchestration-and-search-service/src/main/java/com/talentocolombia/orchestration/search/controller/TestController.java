package com.talentocolombia.orchestration.search.controller;

import com.talentocolombia.jobs.scraper.grpc.JobOffersRequest;
import com.talentocolombia.jobs.scraper.grpc.JobOffersResponse;
import com.talentocolombia.orchestration.search.dto.CandidateProfileDto;
import com.talentocolombia.orchestration.search.dto.JobOfferDto;
import com.talentocolombia.orchestration.search.grpc.JobScraperGrpcClient;
import com.talentocolombia.orchestration.search.service.CandidateProfileClientService;
import com.talentocolombia.orchestration.search.service.MatchingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    private final JobScraperGrpcClient jobScraperGrpcClient;
    private final CandidateProfileClientService candidateProfileClientService;
    private final MatchingService matchingService;

    @Autowired
    public TestController(
            JobScraperGrpcClient jobScraperGrpcClient,
            CandidateProfileClientService candidateProfileClientService,
            MatchingService matchingService) {
        this.jobScraperGrpcClient = jobScraperGrpcClient;
        this.candidateProfileClientService = candidateProfileClientService;
        this.matchingService = matchingService;
    }

    @GetMapping("/jobs-grpc")
    public ResponseEntity<String> getJobOffersFromScraper() {
        try {
            // Construye una solicitud de prueba.
            JobOffersRequest request = JobOffersRequest.newBuilder()
                    .setPage(0)
                    .setSize(10)
                    .build();

            // Llama al cliente gRPC.
            JobOffersResponse response = jobScraperGrpcClient.getJobOffers(request);

            // Si hay ofertas, la conexión fue exitosa.
            if (!response.getJobOffersList().isEmpty()) {
                return ResponseEntity.ok("Conexión gRPC exitosa. Se encontraron " + response.getJobOffersList().size() + " ofertas.");
            } else {
                return ResponseEntity.ok("Conexión gRPC exitosa, pero no se encontraron ofertas.");
            }
        } catch (Exception e) {
            // En caso de error de conexión.
            return ResponseEntity.status(500).body("Error en la conexión gRPC: " + e.getMessage());
        }
    }

//    @GetMapping("/profiles-feign/{id}")
//    public ResponseEntity<CandidateProfileDto> getProfileFromCandidateService(@PathVariable UUID id) {
//        try {
//            CandidateProfileDto profile = candidateProfileClient.getCandidateProfileById(id);
//            return ResponseEntity.ok(profile);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(null);
//        }
//    }

    @GetMapping("/profiles-webclient/{id}") // CAMBIAR EL ENDPOINT PARA EVITAR CONFLICTOS
    public Mono<ResponseEntity<CandidateProfileDto>> getProfileFromCandidateService(@PathVariable UUID id) {
        return candidateProfileClientService.getCandidateProfileById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/profiles-webclient")
    public Mono<ResponseEntity<List<CandidateProfileDto>>> getAllCandidateProfiles() {
        return candidateProfileClientService.getAllCandidateProfiles()
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/matches/{candidateId}")
    public Mono<ResponseEntity<List<JobOfferDto>>> findMatchingJobs(@PathVariable UUID candidateId) {
        return matchingService.findMatchingJobs(candidateId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}