package com.talentocolombia.candidates.profile.repository;

import com.talentocolombia.candidates.profile.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByName(String name);
    // Puedes añadir un método para buscar múltiples habilidades por nombre
    Set<Skill> findByNameIn(Set<String> names);
}