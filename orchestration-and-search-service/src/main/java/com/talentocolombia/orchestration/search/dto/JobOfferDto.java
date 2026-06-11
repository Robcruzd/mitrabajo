package com.talentocolombia.orchestration.search.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class JobOfferDto {
    private String id;
    private String title;
    private String company;
    private String location;
    private String employmentType;
    private String experienceLevel;
    private String description;
    private List<String> skills;
    private String url;
    private String publicationDate;
}