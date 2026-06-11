package com.talentocolombia.candidates.profile.repository;

import com.talentocolombia.candidates.profile.model.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EducationRepository extends JpaRepository<Education, UUID> {
    // List<Education> findByCandidateProfileId(UUID candidateProfileId);
}