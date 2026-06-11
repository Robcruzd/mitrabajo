package com.talentocolombia.candidates.profile.repository;

import com.talentocolombia.candidates.profile.model.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, UUID> {
    // Spring Data JPA automáticamente implementa métodos CRUD.
    // Podemos añadir métodos de consulta personalizados si los necesitamos.
    Optional<CandidateProfile> findByEmail(String email);
    boolean existsByEmail(String email);
}