package com.talentocolombia.gateway.job.graphql.resolver;

import com.talentocolombia.gateway.job.graphql.mapper.JobMapper;
import com.talentocolombia.gateway.job.graphql.model.JobOffer;
import com.talentocolombia.gateway.job.grpc.OrchestrationGrpcClient;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.Collections;
import java.util.List;

@Controller
public class JobQueryResolver {

    private final OrchestrationGrpcClient grpcClient;
    private final JobMapper mapper;

    public JobQueryResolver(
            OrchestrationGrpcClient grpcClient,
            JobMapper mapper) {
        this.grpcClient = grpcClient;
        this.mapper = mapper;
    }

    @QueryMapping
    public List<JobOffer> jobMatchesForCandidate(
            @Argument String candidateId,
            @Argument int page, @Argument int size) {

        return mapper.toGraphqlList(
                grpcClient.jobMatchesForCandidate(candidateId, page, size)
        );
    }

    @QueryMapping
    public List<JobOffer> jobsByKeyword(
            @Argument String keyword,
            @Argument int page, @Argument int size) {

        System.out.println("Received jobsByKeyword query with keyword: " + keyword + ", page: " + page + ", size: " + size);

        try {
            List<JobOffer> jobs = mapper.toGraphqlList(
                    grpcClient.jobsByKeyword(keyword, page, size)
            );

            return jobs != null ? jobs : Collections.emptyList();

        } catch (Exception e) {
            e.printStackTrace();   // IMPORTANTE
            return Collections.emptyList();
        }
    }

    @QueryMapping
    public List<JobOffer> jobsByKeywordAndLocation(
            @Argument String keyword,
            @Argument String location,
            @Argument int page, @Argument int size) {

        return mapper.toGraphqlList(
                grpcClient.jobsByKeywordAndLocation(keyword, location, page, size)
        );
    }
}
