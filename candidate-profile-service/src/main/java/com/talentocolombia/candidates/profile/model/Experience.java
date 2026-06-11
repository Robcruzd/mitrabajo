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
@Table(name = "experiences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"candidateProfile"})
@ToString(exclude = {"candidateProfile"})
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String companyName;

    private String location; // Ej: "Bogotá D.C., Colombia"
    private String employmentType; // Ej: "Full-time", "Part-time"
    private boolean currentlyWorking;

    @Column(nullable = false)
    private Instant startDate;
    private Instant endDate; // Nullable if currentlyWorking is true

    @Column(columnDefinition = "TEXT") // Para descripciones más largas
    private String description;

    @ManyToOne(fetch = FetchType.LAZY) // Muchas experiencias pueden pertenecer a un perfil de candidato
    @JoinColumn(name = "candidate_profile_id", nullable = false)
    private CandidateProfile candidateProfile;
}