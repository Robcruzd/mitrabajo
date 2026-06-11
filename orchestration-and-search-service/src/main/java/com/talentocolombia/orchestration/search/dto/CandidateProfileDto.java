package com.talentocolombia.orchestration.search.dto;

import com.talentocolombia.orchestration.search.model.ExperienceLevel;
import lombok.Data;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class CandidateProfileDto {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String linkedinProfileUrl;
    private String portfolioUrl;
    private String desiredJobTitle;
    private String desiredEmploymentType;
    private String desiredLocation;
    private Double desiredSalaryMin;
    private Double desiredSalaryMax;
    private String desiredSalaryCurrency;
    private ExperienceLevel experienceLevel;
    private Instant createdAt;
    private Instant updatedAt;
    private Set<SkillDto> skills;
    // ... otros campos anidados si los necesitas para la prueba
}