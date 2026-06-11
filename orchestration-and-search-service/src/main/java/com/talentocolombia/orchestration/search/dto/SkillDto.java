package com.talentocolombia.orchestration.search.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class SkillDto {
    private UUID id;
    private String name;
    private String category;
}