package com.talentocolombia.candidates.profile.dto;

import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class EducationDto {
    private UUID id;
    private String institutionName;
    private String degree;
    private String fieldOfStudy;
    private Instant startDate;
    private Instant endDate;
    private String grade;
    private String description;
}