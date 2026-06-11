package com.talentocolombia.orchestration.search.service;

import com.talentocolombia.jobs.scraper.grpc.JobOffer;
import com.talentocolombia.jobs.scraper.grpc.JobOffersRequest;
import com.talentocolombia.jobs.scraper.grpc.JobOffersResponse;
import com.talentocolombia.orchestration.search.dto.CandidateProfileDto;
import com.talentocolombia.orchestration.search.dto.JobOfferDto;
import com.talentocolombia.orchestration.search.grpc.JobScraperGrpcClient;
import com.talentocolombia.orchestration.search.mapper.JobOfferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private static final double MIN_MATCH_SCORE = 30.0; // umbral MVP
    private static final int MAX_RESULTS = 50;

    private final JobScraperGrpcClient jobScraperGrpcClient;
    private final CandidateProfileClientService candidateProfileClientService;

    @Autowired
    public MatchingService(JobScraperGrpcClient jobScraperGrpcClient,
                           CandidateProfileClientService candidateProfileClientService) {
        this.jobScraperGrpcClient = jobScraperGrpcClient;
        this.candidateProfileClientService = candidateProfileClientService;
    }

    public Mono<List<JobOfferDto>> findMatchingJobs(UUID candidateId) {

        Mono<CandidateProfileDto> candidateMono =
                candidateProfileClientService.getCandidateProfileById(candidateId);

        Mono<JobOffersResponse> jobOffersMono =
                Mono.fromCallable(() ->
                        jobScraperGrpcClient.getJobOffers(
                                JobOffersRequest.newBuilder()
                                        .setPage(0)
                                        .setSize(100)
                                        .build()
                        )
                ).subscribeOn(Schedulers.boundedElastic());

        return Mono.zip(candidateMono, jobOffersMono)
                .map(tuple -> {
                    CandidateProfileDto candidate = tuple.getT1();
                    List<JobOffer> rawOffers = tuple.getT2().getJobOffersList();

                    return rawOffers.stream()
                            .map(JobOfferMapper::mapToJobOfferDto)
                            .map(offer -> new ScoredJobOffer(
                                    offer,
                                    calculateMatchScore(candidate, offer)
                            ))
                            .filter(scored -> scored.score >= MIN_MATCH_SCORE)
                            .sorted(Comparator.comparingDouble(ScoredJobOffer::getScore).reversed())
                            .limit(MAX_RESULTS)
                            .map(ScoredJobOffer::getJobOffer)
                            .collect(Collectors.toList());
                });
    }

    /* =========================
       MATCHING LOGIC
       ========================= */

    private double calculateMatchScore(CandidateProfileDto candidate, JobOfferDto offer) {
        double score = 0.0;
        double maxScore = 4.0;

        // 1. Skills (peso fuerte)
        score += calculateSkillsScore(candidate, offer);

        // 2. Título
        score += calculateTitleScore(candidate, offer);

        // 3. Ubicación
        score += calculateLocationScore(candidate, offer);

        // 4. Experiencia
        score += calculateExperienceScore(candidate, offer);

        return (score / maxScore) * 100.0;
    }

    private double calculateSkillsScore(CandidateProfileDto candidate, JobOfferDto offer) {
        if (candidate.getSkills() == null || offer.getSkills() == null) {
            return 0.0;
        }

        Set<String> candidateSkills = candidate.getSkills().stream()
                .map(s -> normalize(s.getName()))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        Set<String> jobSkills = offer.getSkills().stream()
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());

        if (candidateSkills.isEmpty() || jobSkills.isEmpty()) {
            return 0.1; // penaliza ofertas sin skills claras
        }

        long matches = jobSkills.stream()
                .filter(candidateSkills::contains)
                .count();

        return (double) matches / jobSkills.size();
    }

    private double calculateTitleScore(CandidateProfileDto candidate, JobOfferDto offer) {
        if (candidate.getDesiredJobTitle() == null || offer.getTitle() == null) {
            return 0.0;
        }

        Set<String> candidateWords = tokenize(candidate.getDesiredJobTitle());
        Set<String> jobWords = tokenize(offer.getTitle());

        long commonWords = candidateWords.stream()
                .filter(jobWords::contains)
                .count();

        return commonWords > 0 ? 1.0 : 0.0;
    }

    private double calculateLocationScore(CandidateProfileDto candidate, JobOfferDto offer) {
        if (candidate.getDesiredLocation() == null || offer.getLocation() == null) {
            return 0.0;
        }

        String candidateLocation = normalize(candidate.getDesiredLocation());
        String jobLocation = normalize(offer.getLocation());

        return jobLocation.contains(candidateLocation)
                || candidateLocation.contains(jobLocation)
                ? 1.0
                : 0.0;
    }

    private double calculateExperienceScore(CandidateProfileDto candidate, JobOfferDto offer) {
        if (candidate.getExperienceLevel() == null || offer.getExperienceLevel() == null) {
            return 0.0;
        }

        ExperienceLevel candidateLevel = ExperienceLevel.from(candidate.getExperienceLevel().name());
        ExperienceLevel jobLevel = ExperienceLevel.from(offer.getExperienceLevel());

        int diff = Math.abs(candidateLevel.ordinal() - jobLevel.ordinal());

        if (diff == 0) return 1.0;
        if (diff == 1) return 0.5;
        return 0.0;
    }

    /* =========================
       HELPERS
       ========================= */

    private String normalize(String value) {
        return value == null ? "" :
                value.toLowerCase()
                        .replaceAll("[^a-z0-9+.#]", "")
                        .trim();
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase().split("\\s+"))
                .map(this::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toSet());
    }

    /* =========================
       SUPPORT CLASSES
       ========================= */

    private static class ScoredJobOffer {
        private final JobOfferDto jobOffer;
        private final double score;

        public ScoredJobOffer(JobOfferDto jobOffer, double score) {
            this.jobOffer = jobOffer;
            this.score = score;
        }

        public JobOfferDto getJobOffer() {
            return jobOffer;
        }

        public double getScore() {
            return score;
        }
    }

    private enum ExperienceLevel {
        JUNIOR, MID, SENIOR;

        static ExperienceLevel from(String value) {
            String normalized = value.toUpperCase();
            if (normalized.contains("JR")) return JUNIOR;
            if (normalized.contains("SR") || normalized.contains("SENIOR")) return SENIOR;
            return MID;
        }
    }
}
