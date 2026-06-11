package com.talentocolombia.gateway.job.graphql.resolver;

import com.talentocolombia.gateway.job.graphql.mapper.CandidateMapper;
import com.talentocolombia.gateway.job.grpc.OrchestrationGrpcClient;
import com.talentocolombia.orchestration.search.grpc.CandidateMatchResult;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CandidateQueryResolver {

    private final OrchestrationGrpcClient grpcClient;
    private final CandidateMapper candidateMapper;

    public CandidateQueryResolver(OrchestrationGrpcClient grpcClient, CandidateMapper candidateMapper) {
        this.grpcClient = grpcClient;
        this.candidateMapper = candidateMapper;
    }

    public List<CandidateMatchResult> candidatesForJob(
            String jobId, int page, int size) {

        return grpcClient.candidatesForJob(jobId, page, size);
    }

    @QueryMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public List<com.talentocolombia.gateway.job.graphql.model.CandidateMatch> candidatesForJobAuth(
            @Argument String jobId,
            @Argument int page, @Argument int size) {
        return candidateMapper.toGraphqlList(grpcClient.candidatesForJob(jobId, page, size));
    }

}
