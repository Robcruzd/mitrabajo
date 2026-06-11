package com.talentocolombia.orchestration.search.mapper;

import com.talentocolombia.jobs.scraper.grpc.JobOffer;
import com.talentocolombia.orchestration.search.dto.JobOfferDto;

public class JobOfferMapper {

    private JobOfferMapper() {
        // Utility class, prevent instantiation
    }

    public static JobOfferDto mapToJobOfferDto(JobOffer proto) {
        JobOfferDto dto = new JobOfferDto();
        dto.setId(proto.getId());
        dto.setTitle(proto.getTitle());
        dto.setCompany(proto.getCompanyName());
        dto.setLocation(proto.getLocation().toString());
        dto.setEmploymentType(proto.getEmploymentType());
        dto.setDescription(proto.getDescription());
        dto.setUrl(proto.getUrl());
        dto.setPublicationDate(proto.getPublicationDate().toString());
        dto.setSkills(proto.getRequiredSkillsList());
        dto.setExperienceLevel(proto.getExperienceLevel());
        return dto;
    }
}