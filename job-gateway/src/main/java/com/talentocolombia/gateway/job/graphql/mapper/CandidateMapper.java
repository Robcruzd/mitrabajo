package com.talentocolombia.gateway.job.graphql.mapper;

import com.talentocolombia.gateway.job.graphql.model.CandidateMatch;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CandidateMapper {

    CandidateMatch toGraphql(
            com.talentocolombia.orchestration.search.grpc.CandidateMatchResult grpc
    );

    List<CandidateMatch> toGraphqlList(
            List<com.talentocolombia.orchestration.search.grpc.CandidateMatchResult> grpcList
    );
}
