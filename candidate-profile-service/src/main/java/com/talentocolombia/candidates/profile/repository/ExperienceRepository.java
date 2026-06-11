package com.talentocolombia.candidates.profile.repository;

import com.talentocolombia.candidates.profile.model.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, UUID> {
    // Spring Data JPA te permite encontrar experiencias por el ID del candidato
    // List<Experience> findByCandidateProfileId(UUID candidateProfileId);
}