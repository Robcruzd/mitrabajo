package com.talentocolombia.orchestration.search.service;

import com.talentocolombia.orchestration.search.dto.JobOfferDto;
import com.talentocolombia.orchestration.search.grpc.CandidateMatchesResponse;
import com.talentocolombia.orchestration.search.grpc.JobMatchResult;
import com.talentocolombia.orchestration.search.grpc.JobMatchesResponse;
import com.talentocolombia.orchestration.search.grpc.OrchestrationSearchServiceGrpc;
import com.talentocolombia.orchestration.search.grpc.SearchCandidatesForJobRequest;
import com.talentocolombia.orchestration.search.grpc.SearchJobsByKeywordAndLocationRequest;
import com.talentocolombia.orchestration.search.grpc.SearchJobsByKeywordRequest;
import com.talentocolombia.orchestration.search.grpc.SearchJobsForCandidateRequest;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
public class OrchestrationSearchGrpcService
        extends OrchestrationSearchServiceGrpc
        .OrchestrationSearchServiceImplBase {

    private final MatchingService matchingService;
    private final CandidateProfileClientService candidateService;
    private final OffersClientService offersClientService;

    public OrchestrationSearchGrpcService(
            MatchingService matchingService,
            CandidateProfileClientService candidateService,
            OffersClientService offersClientService) {
        this.matchingService = matchingService;
        this.candidateService = candidateService;
        this.offersClientService = offersClientService;
    }

    /* ======================
       JOBS FOR CANDIDATE
       ====================== */

    @Override
    public void findJobsForCandidate(
            SearchJobsForCandidateRequest request,
            StreamObserver<JobMatchesResponse> responseObserver) {

        UUID candidateId = UUID.fromString(request.getCandidateId());

        matchingService.findMatchingJobs(candidateId)
                .map(jobs -> jobs.stream()
                        .map(this::mapToJobMatchResult)
                        .collect(Collectors.toList())
                )
                .subscribe(
                        results -> {
                            JobMatchesResponse response =
                                    JobMatchesResponse.newBuilder()
                                            .addAllJobs(results)
                                            .build();

                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        responseObserver::onError
                );
    }

    /* ======================
       CANDIDATES FOR JOB (OPTIONAL MVP)
       ====================== */

    @Override
    public void findCandidatesForJob(
            SearchCandidatesForJobRequest request,
            StreamObserver<CandidateMatchesResponse> responseObserver) {

        // MVP: NO IMPLEMENTAR COMPLEJO
        responseObserver.onError(
                Status.UNIMPLEMENTED
                        .withDescription("Not implemented in MVP")
                        .asRuntimeException()
        );
    }

    @Override
    public void findJobsByKeywordAndLocation(
            SearchJobsByKeywordAndLocationRequest request,
            StreamObserver<JobMatchesResponse> responseObserver) {

        offersClientService.findJobsByKeywordAndLocation(
                        request.getKeyword(),
                        request.getLocation()
                )
                .map(jobs -> jobs.stream()
                        .map(this::mapToJobMatchResult)
                        .toList()
                )
                .subscribe(
                        results -> {
                            responseObserver.onNext(
                                    JobMatchesResponse.newBuilder()
                                            .addAllJobs(results)
                                            .build()
                            );
                            responseObserver.onCompleted();
                        },
                        responseObserver::onError
                );
    }

    @Override
    public void findJobsByKeyword(
            SearchJobsByKeywordRequest request,
            StreamObserver<JobMatchesResponse> responseObserver) {

        offersClientService.findJobsByKeyword(request.getKeyword())
                .map(jobs -> jobs.stream()
                        .map(this::mapToJobMatchResult)
                        .toList()
                )
                .subscribe(
                        results -> {
                            responseObserver.onNext(
                                    JobMatchesResponse.newBuilder()
                                            .addAllJobs(results)
                                            .build()
                            );
                            responseObserver.onCompleted();
                        },
                        responseObserver::onError
                );
    }

    /* ======================
       MAPPING
       ====================== */

    private JobMatchResult mapToJobMatchResult(JobOfferDto job) {
        return JobMatchResult.newBuilder()
                .setJobId(job.getId())
                .setTitle(job.getTitle())
                .setCompany(job.getCompany())
                .setLocation(job.getLocation())
                .setExperienceLevel(job.getExperienceLevel())
                //.setScore(job.getScore()) // viene del MatchingService
                .setUrl(job.getUrl())
                .build();
    }
}
