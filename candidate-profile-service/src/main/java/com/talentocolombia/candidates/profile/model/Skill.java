package com.talentocolombia.candidates.profile.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"candidateProfiles"}) // Excluir relaciones bidireccionales en equals/hashCode
@ToString(exclude = {"candidateProfiles"}) // Excluir relaciones para evitar bucles infinitos
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name; // Ej: "Java", "Spring Boot", "Kubernetes"

    @Column(nullable = false)
    private String category; // Ej: "Programming Languages", "Cloud Technologies", "Soft Skills"

    @ManyToMany(mappedBy = "skills", fetch = FetchType.LAZY) // MappedBy indica la relación inversa en CandidateProfile
    private Set<CandidateProfile> candidateProfiles = new HashSet<>();
}