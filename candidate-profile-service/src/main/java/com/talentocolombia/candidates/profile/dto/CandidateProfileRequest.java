package com.talentocolombia.candidates.profile.dto;

import com.talentocolombia.candidates.profile.model.ExperienceLevel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Set;

@Data
public class CandidateProfileRequest {
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name is mandatory")
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    private String lastName;

    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "Phone number is invalid")
    private String phoneNumber;
    private String linkedinProfileUrl;
    private String portfolioUrl;
    private String desiredJobTitle;
    private String desiredEmploymentType;
    private String desiredLocation;
    private Double desiredSalaryMin;
    private Double desiredSalaryMax;
    private String desiredSalaryCurrency;

    @NotNull(message = "Experience level is mandatory")
    private ExperienceLevel experienceLevel;

    // Campos para relaciones anidadas (se pueden incluir o manejar por separado)
    // Para simplificar el MVP, manejaremos experiencias, educación y habilidades por separado
    // o con DTOs de request anidados si se desea crear todo en una sola request.
    // Aquí los dejamos opcionales para una creación/actualización inicial.
    private Set<ExperienceRequest> experiences;
    private Set<EducationRequest> educations;
    private Set<String> skillNames; // Solo necesitamos los nombres de las habilidades
}