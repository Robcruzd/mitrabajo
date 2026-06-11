package com.talentocolombia.candidates.profile.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID; // Para usar UUID como ID

@Entity
@Table(name = "candidate_profiles")
@Data // Lombok para getters, setters, equals, hashCode, toString
@NoArgsConstructor // Lombok para constructor sin argumentos
@AllArgsConstructor // Lombok para constructor con todos los argumentos (útil para pruebas o mapeo)
@EqualsAndHashCode(exclude = {"experiences", "educations", "skills"})
@ToString(exclude = {"experiences", "educations", "skills"})
public class CandidateProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Generación de UUID automática
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email; // Usaremos el email como un identificador único para el usuario

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phoneNumber;
    private String linkedinProfileUrl;
    private String portfolioUrl;
    private String desiredJobTitle;
    private String desiredEmploymentType; // Ej: Full-time, Part-time, Contract, Freelance
    private String desiredLocation; // Ciudad, Departamento, o "Remoto"
    private Double desiredSalaryMin; // Salario mínimo deseado
    private Double desiredSalaryMax; // Salario máximo deseado
    private String desiredSalaryCurrency; // Ej: COP, USD

    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como string en la DB
    private ExperienceLevel experienceLevel; // Enum para nivel de experiencia

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Relaciones:

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Experience> experiences = new HashSet<>();

    @OneToMany(mappedBy = "candidateProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Education> educations = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "candidate_profile_skills",
            joinColumns = @JoinColumn(name = "candidate_profile_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    // Callbacks de ciclo de vida para auditar fechas
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}