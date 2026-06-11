package com.talentocolombia.candidates.profile.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class ExperienceDto {
    private UUID id;
    private String jobTitle;
    private String companyName;
    private String location;
    private String employmentType;
    private boolean currentlyWorking;
    private Instant startDate;
    private Instant endDate;
    private String description;
}