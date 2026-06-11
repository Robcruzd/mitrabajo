package com.talentocolombia.candidates.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class ExperienceRequest {
    private UUID id; // Para actualizaciones
    @NotBlank(message = "Job title is mandatory")
    private String jobTitle;
    @NotBlank(message = "Company name is mandatory")
    private String companyName;
    private String location;
    private String employmentType;
    private boolean currentlyWorking;
    @NotNull(message = "Start date is mandatory")
    private Instant startDate;
    private Instant endDate;
    private String description;
}