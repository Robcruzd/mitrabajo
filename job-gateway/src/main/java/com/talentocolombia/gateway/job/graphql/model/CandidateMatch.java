package com.talentocolombia.gateway.job.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class CandidateMatch {

    private String candidateId;
    private String name;
    private Float score;
    private List<String> matchedSkills;
}
