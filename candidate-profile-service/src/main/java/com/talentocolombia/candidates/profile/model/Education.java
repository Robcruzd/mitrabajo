package com.talentocolombia.candidates.profile.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "educations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"candidateProfile"})
@ToString(exclude = {"candidateProfile"})
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String degree; // Ej: "Ingeniería de Sistemas", "Maestría en IA"

    private String fieldOfStudy;

    @Column(nullable = false)
    private Instant startDate;
    private Instant endDate; // Nullable if still studying

    private String grade; // Ej: "Magna Cum Laude", "4.5/5.0"
    private String description; // Detalles adicionales

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_profile_id", nullable = false)
    private CandidateProfile candidateProfile;
}