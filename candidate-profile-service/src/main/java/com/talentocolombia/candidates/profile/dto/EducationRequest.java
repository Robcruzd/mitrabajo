package com.talentocolombia.candidates.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data
public class EducationRequest {
    private UUID id; // Para actualizaciones
    @NotBlank(message = "Institution name is mandatory")
    private String institutionName;
    @NotBlank(message = "Degree is mandatory")
    private String degree;
    private String fieldOfStudy;
    @NotNull(message = "Start date is mandatory")
    private Instant startDate;
    private Instant endDate;
    private String grade;
    private String description;
}