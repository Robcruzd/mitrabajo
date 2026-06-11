package com.talentocolombia.gateway.job.grpc;

import com.talentocolombia.orchestration.search.grpc.CandidateMatchResult;
import com.talentocolombia.orchestration.search.grpc.JobMatchResult;
import com.talentocolombia.orchestration.search.grpc.OrchestrationSearchServiceGrpc;
import com.talentocolombia.orchestration.search.grpc.SearchCandidatesForJobRequest;
import com.talentocolombia.orchestration.search.grpc.SearchJobsByKeywordAndLocationRequest;
import com.talentocolombia.orchestration.search.grpc.SearchJobsByKeywordRequest;
import com.talentocolombia.orchestration.search.grpc.SearchJobsForCandidateRequest;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrchestrationGrpcClient {

    private final OrchestrationSearchServiceGrpc
            .OrchestrationSearchServiceBlockingStub stub;

    public OrchestrationGrpcClient(
            OrchestrationSearchServiceGrpc
                    .OrchestrationSearchServiceBlockingStub stub) {
        this.stub = stub;
    }

    public List<JobMatchResult> jobMatchesForCandidate(
            String candidateId, int page, int size) {

        SearchJobsForCandidateRequest request =
                SearchJobsForCandidateRequest.newBuilder()
                        .setCandidateId(candidateId)
                        .setPage(page)
                        .setSize(size)
                        .build();

        return stub.findJobsForCandidate(request)
                .getJobsList();
    }

    public List<JobMatchResult> jobsByKeyword(
            String keyword, int page, int size) {

        SearchJobsByKeywordRequest request =
                SearchJobsByKeywordRequest.newBuilder()
                        .setKeyword(keyword)
                        .setPage(page)
                        .setSize(size)
                        .build();

        return stub.findJobsByKeyword(request)
                .getJobsList();
    }

    public List<JobMatchResult> jobsByKeywordAndLocation(
            String keyword, String location, int page, int size) {

        SearchJobsByKeywordAndLocationRequest request =
                SearchJobsByKeywordAndLocationRequest.newBuilder()
                        .setKeyword(keyword)
                        .setLocation(location)
                        .setPage(page)
                        .setSize(size)
                        .build();

        return stub.findJobsByKeywordAndLocation(request)
                .getJobsList();
    }

    public List<CandidateMatchResult> candidatesForJob(
            String jobId, int page, int size) {

        SearchCandidatesForJobRequest request =
                SearchCandidatesForJobRequest.newBuilder()
                        .setJobId(jobId)
                        .setPage(page)
                        .setSize(size)
                        .build();

        return stub.findCandidatesForJob(request)
                .getCandidatesList();
    }
}
