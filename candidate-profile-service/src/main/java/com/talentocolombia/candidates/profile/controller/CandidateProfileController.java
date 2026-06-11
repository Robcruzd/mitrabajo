package com.talentocolombia.candidates.profile.controller;

import com.talentocolombia.candidates.profile.dto.CandidateProfileDto;
import com.talentocolombia.candidates.profile.dto.CandidateProfileRequest;
import com.talentocolombia.candidates.profile.service.CandidateProfileService;
import jakarta.validation.Valid; // Para la validación de DTOs
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController // Indica que es un controlador REST
@RequestMapping("/api/v1/candidates") // Define la ruta base para este controlador
public class CandidateProfileController {

    private final CandidateProfileService candidateProfileService;

    @Autowired
    public CandidateProfileController(CandidateProfileService candidateProfileService) {
        this.candidateProfileService = candidateProfileService;
    }

    @GetMapping
    public ResponseEntity<List<CandidateProfileDto>> getAllCandidateProfiles() {
        List<CandidateProfileDto> profiles = candidateProfileService.getAllCandidateProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CandidateProfileDto> getCandidateProfileById(@PathVariable UUID id) {
        CandidateProfileDto profile = candidateProfileService.getCandidateProfileById(id);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/by-email")
    public ResponseEntity<CandidateProfileDto> getCandidateProfileByEmail(@RequestParam String email) {
        CandidateProfileDto profile = candidateProfileService.getCandidateProfileByEmail(email);
        return ResponseEntity.ok(profile);
    }

    @PostMapping
    public ResponseEntity<CandidateProfileDto> createCandidateProfile(@Valid @RequestBody CandidateProfileRequest request) {
        CandidateProfileDto createdProfile = candidateProfileService.createCandidateProfile(request);
        return new ResponseEntity<>(createdProfile, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CandidateProfileDto> updateCandidateProfile(@PathVariable UUID id, @Valid @RequestBody CandidateProfileRequest request) {
        CandidateProfileDto updatedProfile = candidateProfileService.updateCandidateProfile(id, request);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCandidateProfile(@PathVariable UUID id) {
        candidateProfileService.deleteCandidateProfile(id);
        return ResponseEntity.noContent().build();
    }
}