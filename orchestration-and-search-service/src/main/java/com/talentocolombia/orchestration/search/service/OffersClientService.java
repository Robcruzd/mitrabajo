package com.talentocolombia.orchestration.search.service;

import com.talentocolombia.jobs.scraper.grpc.JobOffersRequest;
import com.talentocolombia.jobs.scraper.grpc.JobOffersResponse;
import com.talentocolombia.orchestration.search.dto.JobOfferDto;
import com.talentocolombia.orchestration.search.grpc.JobScraperGrpcClient;
import com.talentocolombia.orchestration.search.mapper.JobOfferMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;

@Service
public class OffersClientService {

    private final JobScraperGrpcClient jobScraperGrpcClient;

    @Autowired
    public OffersClientService(JobScraperGrpcClient jobScraperGrpcClient) {
        this.jobScraperGrpcClient = jobScraperGrpcClient;
    }

    public Mono<List<JobOfferDto>> findJobsBySkills(List<String> skills) {
        Mono<JobOffersResponse> jobOffersMono =
                Mono.fromCallable(() ->
                        jobScraperGrpcClient.getJobOffers(
                                JobOffersRequest.newBuilder()
                                        .setPage(0)
                                        .setSize(100)
                                        .build()
                        )
                ).subscribeOn(Schedulers.boundedElastic());
        return jobOffersMono.map(response -> response.getJobOffersList().stream()
                .map(JobOfferMapper::mapToJobOfferDto)
                .filter(offer -> offerMatchesSkills(offer, skills))
                .toList());
    }

    public Mono<List<JobOfferDto>> findJobsByKeyword(String keyword) {
        Mono<JobOffersResponse> jobOffersMono =
                Mono.fromCallable(() ->
                        jobScraperGrpcClient.getJobOffers(
                                JobOffersRequest.newBuilder()
                                        .setQuery(keyword)
                                        .setPage(0)
                                        .setSize(100)
                                        .build()
                        )
                ).subscribeOn(Schedulers.boundedElastic());
        return jobOffersMono.map(response -> response.getJobOffersList().stream()
                .map(JobOfferMapper::mapToJobOfferDto)
                .toList());
    }

    public Mono<List<JobOfferDto>> getAllJobOffers() {
        Mono<JobOffersResponse> jobOffersMono =
                Mono.fromCallable(() ->
                        jobScraperGrpcClient.getJobOffers(
                                JobOffersRequest.newBuilder()
                                        .setPage(0)
                                        .setSize(100)
                                        .build()
                        )
                ).subscribeOn(Schedulers.boundedElastic());
        return jobOffersMono.map(response -> response.getJobOffersList().stream()
                .map(JobOfferMapper::mapToJobOfferDto)
                .toList());
    }

    public Mono<List<JobOfferDto>> findJobsByKeywordAndLocation(
            String keyword,
            String location) {
        Mono<JobOffersResponse> jobOffersMono =
                Mono.fromCallable(() ->
                        jobScraperGrpcClient.getJobOffers(
                                JobOffersRequest.newBuilder()
                                        .setQuery(keyword)
                                        .setLocation(location)
                                        .setPage(0)
                                        .setSize(100)
                                        .build()
                        )
                ).subscribeOn(Schedulers.boundedElastic());
        return jobOffersMono.map(response -> response.getJobOffersList().stream()
                .map(JobOfferMapper::mapToJobOfferDto)
                .toList());
    }

    private static boolean offerMatchesSkills(JobOfferDto offer, List<String> skills) {
        if (skills == null || skills.isEmpty()) {
            return true;
        }
        if (offer.getSkills() == null) {
            return false;
        }
        return new HashSet<>(offer.getSkills()).containsAll(skills);
    }
}
