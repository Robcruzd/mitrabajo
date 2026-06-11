package com.talentocolombia.jobs.scraper.repository;

import com.talentocolombia.jobs.scraper.model.JobOffer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface JobOfferRepository extends ReactiveMongoRepository<JobOffer, String> {

    // Devuelve un Mono<JobOffer> porque esperamos 0 o 1 resultado
    Mono<JobOffer> findBySourcePlatformAndExternalId(String sourcePlatform, String externalId);

    // Puedes añadir otros métodos reactivos según sea necesario
}