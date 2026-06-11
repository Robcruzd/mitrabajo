package com.talentocolombia.gateway.job.graphql.mapper;

import com.talentocolombia.gateway.job.graphql.model.JobOffer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface JobMapper {

    JobOffer toGraphql(
            com.talentocolombia.orchestration.search.grpc.JobMatchResult grpc
    );

    List<JobOffer> toGraphqlList(
            List<com.talentocolombia.orchestration.search.grpc.JobMatchResult> grpcList
    );
}
