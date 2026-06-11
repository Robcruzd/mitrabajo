package com.talentocolombia.jobs.scraper.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.util.List;

@Data
@Document(collection = "job_offers")
@CompoundIndexes({
        @CompoundIndex(name = "source_external_id_idx", def = "{'sourcePlatform': 1, 'externalId': 1}", unique = true)
})
public class JobOffer {

    @Id
    private String id;

    private String externalId;
    @Indexed
    private String sourcePlatform;
    private String url;
    @Indexed(direction = org.springframework.data.mongodb.core.index.IndexDirection.ASCENDING)
    private String title;
    private String companyName;
    private String companyLogoUrl;

    private Location location;
    private String description;
    private SalaryRange salaryRange;
    private String employmentType;
    private String experienceLevel;

    @Indexed(direction = org.springframework.data.mongodb.core.index.IndexDirection.DESCENDING)
    private Instant publicationDate;
    private Instant expirationDate;
    private Instant applicationDeadline;
    private String contactEmail;

    @Indexed
    private List<String> requiredSkills;
    private List<String> benefits;

    @Indexed(direction = org.springframework.data.mongodb.core.index.IndexDirection.DESCENDING)
    private Instant crawledAt;
    private boolean isActive;
    private List<String> jobFunctions;

    @Data
    public static class Location {
        private String city;
        private String department;
        private String country;
    }

    @Data
    public static class SalaryRange {
        private Double min;
        private Double max;
        private String currency;
        private String text;
    }
}