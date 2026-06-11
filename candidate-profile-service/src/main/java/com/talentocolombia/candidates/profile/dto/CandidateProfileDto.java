package com.talentocolombia.candidates.profile.dto;

import com.talentocolombia.candidates.profile.model.ExperienceLevel;
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
    private Set<ExperienceDto> experiences;
    private Set<EducationDto> educations;
    private Set<SkillDto> skills;
}