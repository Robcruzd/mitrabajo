package com.talentocolombia.candidates.profile.service;

import com.talentocolombia.candidates.profile.dto.*;
import com.talentocolombia.candidates.profile.exception.ResourceNotFoundException;
import com.talentocolombia.candidates.profile.model.CandidateProfile;
import com.talentocolombia.candidates.profile.model.Education;
import com.talentocolombia.candidates.profile.model.Experience;
import com.talentocolombia.candidates.profile.model.Skill;
import com.talentocolombia.candidates.profile.repository.CandidateProfileRepository;
import com.talentocolombia.candidates.profile.repository.EducationRepository;
import com.talentocolombia.candidates.profile.repository.ExperienceRepository;
import com.talentocolombia.candidates.profile.repository.SkillRepository;
import org.springframework.beans.BeanUtils; // Utilidad para copiar propiedades
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Para manejo de transacciones

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository candidateProfileRepository;
    private final SkillRepository skillRepository;
    private final ExperienceRepository experienceRepository;
    private final EducationRepository educationRepository;

    @Autowired
    public CandidateProfileService(CandidateProfileRepository candidateProfileRepository,
                                   SkillRepository skillRepository,
                                   ExperienceRepository experienceRepository,
                                   EducationRepository educationRepository) {
        this.candidateProfileRepository = candidateProfileRepository;
        this.skillRepository = skillRepository;
        this.experienceRepository = experienceRepository;
        this.educationRepository = educationRepository;
    }

    @Transactional(readOnly = true)
    public List<CandidateProfileDto> getAllCandidateProfiles() {
        return candidateProfileRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CandidateProfileDto getCandidateProfileById(UUID id) {
        CandidateProfile profile = candidateProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CandidateProfile not found with id " + id));
        return mapToDto(profile);
    }

    @Transactional(readOnly = true)
    public CandidateProfileDto getCandidateProfileByEmail(String email) {
        CandidateProfile profile = candidateProfileRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("CandidateProfile not found with email " + email));
        return mapToDto(profile);
    }

    @Transactional
    public CandidateProfileDto createCandidateProfile(CandidateProfileRequest request) {
        if (candidateProfileRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Candidate profile with this email already exists: " + request.getEmail());
        }

        CandidateProfile profile = new CandidateProfile();
        // Usar BeanUtils para copiar propiedades comunes, luego mapear relaciones
        BeanUtils.copyProperties(request, profile, "experiences", "educations", "skillNames");

        // Setear las fechas de creación y actualización (aunque @PrePersist lo hace, es explícito)
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());

        // Manejar habilidades
        if (request.getSkillNames() != null && !request.getSkillNames().isEmpty()) {
            Set<Skill> skills = new HashSet<>();
            for (String skillName : request.getSkillNames()) {
                Skill skill = skillRepository.findByName(skillName)
                        .orElseGet(() -> {
                            // Si la habilidad no existe, crearla y guardarla
                            Skill newSkill = new Skill();
                            newSkill.setName(skillName);
                            newSkill.setCategory("General"); // Categoría por defecto, puede ser mejorada
                            return skillRepository.save(newSkill);
                        });
                skills.add(skill);
            }
            profile.setSkills(skills);
        }

        // Guardar el perfil primero para obtener un ID, si es necesario para relaciones bidireccionales
        // Aunque con cascade = ALL y orphanRemoval=true, JPA puede manejarlo al final.
        CandidateProfile savedProfile = candidateProfileRepository.save(profile);

        // Manejar experiencias
        if (request.getExperiences() != null && !request.getExperiences().isEmpty()) {
            Set<Experience> experiences = request.getExperiences().stream()
                    .map(expReq -> {
                        Experience experience = new Experience();
                        BeanUtils.copyProperties(expReq, experience);
                        experience.setCandidateProfile(savedProfile);
                        return experience;
                    })
                    .collect(Collectors.toSet());
            savedProfile.setExperiences(experiences);
        }

        // Manejar educación
        if (request.getEducations() != null && !request.getEducations().isEmpty()) {
            Set<Education> educations = request.getEducations().stream()
                    .map(eduReq -> {
                        Education education = new Education();
                        BeanUtils.copyProperties(eduReq, education);
                        education.setCandidateProfile(savedProfile);
                        return education;
                    })
                    .collect(Collectors.toSet());
            savedProfile.setEducations(educations);
        }

        // Guardar de nuevo para que las relaciones anidadas con cascade sean persistidas
        // (Aunque para OneToMany con CascadeType.ALL en CandidateProfile, se maneja automáticamente)
        return mapToDto(candidateProfileRepository.save(savedProfile));
    }

    @Transactional
    public CandidateProfileDto updateCandidateProfile(UUID id, CandidateProfileRequest request) {
        CandidateProfile existingProfile = candidateProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CandidateProfile not found with id " + id));

        // Actualizar propiedades básicas
        BeanUtils.copyProperties(request, existingProfile, "id", "createdAt", "experiences", "educations", "skillNames");
        existingProfile.setUpdatedAt(Instant.now());

        // Manejar actualización de habilidades
        if (request.getSkillNames() != null) {
            Set<Skill> newSkills = new HashSet<>();
            for (String skillName : request.getSkillNames()) {
                Skill skill = skillRepository.findByName(skillName)
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(skillName);
                            newSkill.setCategory("General");
                            return skillRepository.save(newSkill);
                        });
                newSkills.add(skill);
            }
            existingProfile.setSkills(newSkills); // Reemplaza el conjunto de habilidades existente
        }

        // Manejar actualización de experiencias (lógica más compleja para add/update/remove)
        // Para MVP, una estrategia simple: limpiar y recrear, o manejar por IDs.
        // Aquí usaremos una estrategia de sincronización:
        syncExperiences(existingProfile, request.getExperiences());

        // Manejar actualización de educación
        syncEducations(existingProfile, request.getEducations());


        return mapToDto(candidateProfileRepository.save(existingProfile));
    }

    @Transactional
    public void deleteCandidateProfile(UUID id) {
        if (!candidateProfileRepository.existsById(id)) {
            throw new ResourceNotFoundException("CandidateProfile not found with id " + id);
        }
        candidateProfileRepository.deleteById(id);
    }

    // --- Métodos Helper para mapeo entre Entidad y DTO ---

    private CandidateProfileDto mapToDto(CandidateProfile profile) {
        CandidateProfileDto dto = new CandidateProfileDto();
        BeanUtils.copyProperties(profile, dto);

        // Mapear relaciones anidadas
        if (profile.getExperiences() != null) {
            dto.setExperiences(profile.getExperiences().stream()
                    .map(this::mapExperienceToDto)
                    .collect(Collectors.toSet()));
        }
        if (profile.getEducations() != null) {
            dto.setEducations(profile.getEducations().stream()
                    .map(this::mapEducationToDto)
                    .collect(Collectors.toSet()));
        }
        if (profile.getSkills() != null) {
            dto.setSkills(profile.getSkills().stream()
                    .map(this::mapSkillToDto)
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    private ExperienceDto mapExperienceToDto(Experience experience) {
        ExperienceDto dto = new ExperienceDto();
        BeanUtils.copyProperties(experience, dto);
        return dto;
    }

    private EducationDto mapEducationToDto(Education education) {
        EducationDto dto = new EducationDto();
        BeanUtils.copyProperties(education, dto);
        return dto;
    }

    private SkillDto mapSkillToDto(Skill skill) {
        SkillDto dto = new SkillDto();
        BeanUtils.copyProperties(skill, dto);
        return dto;
    }

    // --- Métodos de sincronización para relaciones Many-to-Many y One-to-Many ---
    // Esto es más robusto que solo reemplazar la colección.

    private void syncExperiences(CandidateProfile profile, Set<ExperienceRequest> newExperienceRequests) {
        Set<Experience> currentExperiences = profile.getExperiences();
        Set<Experience> updatedExperiences = new HashSet<>();

        if (newExperienceRequests != null) {
            for (ExperienceRequest req : newExperienceRequests) {
                if (req.getId() != null) {
                    // Intenta encontrar una experiencia existente por ID
                    currentExperiences.stream()
                            .filter(exp -> exp.getId().equals(req.getId()))
                            .findFirst()
                            .ifPresentOrElse(existingExp -> {
                                BeanUtils.copyProperties(req, existingExp, "id"); // No copiar ID
                                updatedExperiences.add(existingExp);
                            }, () -> {
                                // Si el ID se proporciona pero no se encuentra, podría ser un error o una nueva adición
                                // Para simplificar: si no se encuentra por ID, se crea una nueva.
                                Experience newExp = new Experience();
                                BeanUtils.copyProperties(req, newExp);
                                newExp.setCandidateProfile(profile);
                                updatedExperiences.add(newExp);
                            });
                } else {
                    // Nueva experiencia
                    Experience newExp = new Experience();
                    BeanUtils.copyProperties(req, newExp);
                    newExp.setCandidateProfile(profile);
                    updatedExperiences.add(newExp);
                }
            }
        }

        // Limpiar las experiencias existentes que no estén en la nueva lista
        currentExperiences.retainAll(updatedExperiences);
        // Añadir las nuevas experiencias o las actualizadas al conjunto original
        currentExperiences.addAll(updatedExperiences);
    }

    private void syncEducations(CandidateProfile profile, Set<EducationRequest> newEducationRequests) {
        Set<Education> currentEducations = profile.getEducations();
        Set<Education> updatedEducations = new HashSet<>();

        if (newEducationRequests != null) {
            for (EducationRequest req : newEducationRequests) {
                if (req.getId() != null) {
                    currentEducations.stream()
                            .filter(edu -> edu.getId().equals(req.getId()))
                            .findFirst()
                            .ifPresentOrElse(existingEdu -> {
                                BeanUtils.copyProperties(req, existingEdu, "id");
                                updatedEducations.add(existingEdu);
                            }, () -> {
                                Education newEdu = new Education();
                                BeanUtils.copyProperties(req, newEdu);
                                newEdu.setCandidateProfile(profile);
                                updatedEducations.add(newEdu);
                            });
                } else {
                    Education newEdu = new Education();
                    BeanUtils.copyProperties(req, newEdu);
                    newEdu.setCandidateProfile(profile);
                    updatedEducations.add(newEdu);
                }
            }
        }

        currentEducations.retainAll(updatedEducations);
        currentEducations.addAll(updatedEducations);
    }

    // Clase de excepción personalizada para recursos no encontrados
    // Ruta: candidate-profile-service/src/main/java/com/talentocolombia/candidates/profile/exception/ResourceNotFoundException.java
    // (Necesitas crear este archivo)
}