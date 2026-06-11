package com.talentocolombia.gateway.job.graphql.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JobOffer {

    private String jobId;
    private String title;
    private String company;
    private String location;
    private String experienceLevel;
    private Float score;
    private String url;
}
