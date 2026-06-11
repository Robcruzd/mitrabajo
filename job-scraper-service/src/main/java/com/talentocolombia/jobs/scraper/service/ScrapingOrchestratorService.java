package com.talentocolombia.jobs.scraper.service;

import com.talentocolombia.jobs.scraper.model.JobOffer;
import com.talentocolombia.jobs.scraper.repository.JobOfferRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScrapingOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(ScrapingOrchestratorService.class);

    private final JobOfferRepository jobOfferRepository;
    private final Map<String, JobScraper> scrapers; // Mapa de nombre de plataforma a scraper

    @Autowired
    public ScrapingOrchestratorService(JobOfferRepository jobOfferRepository, List<JobScraper> allScrapers) {
        this.jobOfferRepository = jobOfferRepository;
        // Mapea todos los JobScraper inyectados por Spring por su nombre de plataforma
        this.scrapers = allScrapers.stream()
                .collect(Collectors.toMap(JobScraper::getPlatformName, Function.identity()));
        log.info("Initialized with scrapers: {}", scrapers.keySet());
    }

    /**
     * Inicia el proceso de scraping para una plataforma específica y guarda las ofertas.
     * @param platformName El nombre de la plataforma a scrapear (ej. "Computrabajo").
     * @param query Término de búsqueda.
     * @param location Ubicación de búsqueda.
     * @param maxPages Número máximo de páginas a scrapear.
     * @return Mono<Integer> El número de ofertas nuevas/actualizadas guardadas.
     */
    public Mono<Integer> scrapeAndSave(String platformName, String query, String location, int maxPages) {
        JobScraper scraper = scrapers.get(platformName);
        if (scraper == null) {
            log.error("No scraper found for platform: {}", platformName);
            return Mono.error(new IllegalArgumentException("No scraper found for platform: " + platformName));
        }

        log.info("Starting scrape for platform: {} with query '{}', location '{}'", platformName, query, location);

        return scraper.scrapeJobs(query, location, maxPages)
                .flatMap(this::saveOrUpdateJobOffer) // Procesar cada oferta de forma reactiva
                .doOnNext(saved -> log.debug("Saved/Updated job: {}", saved.getTitle()))
                .count() // Contar el número de elementos procesados
                .map(Long::intValue) // Convertir a Integer
                .doOnSuccess(count -> log.info("Finished scraping {}. Saved/Updated {} offers.", platformName, count))
                .doOnError(e -> log.error("Error during scraping process: {}", e.getMessage()));
    }

    /**
     * Guarda una oferta de empleo si es nueva, o la actualiza si ya existe (basado en sourcePlatform y externalId).
     * @param jobOffer La oferta de empleo a guardar/actualizar.
     * @return Mono<JobOffer> La oferta de empleo guardada o actualizada.
     */
    private Mono<JobOffer> saveOrUpdateJobOffer(JobOffer jobOffer) {
        if (jobOffer.getExternalId() == null || jobOffer.getSourcePlatform() == null) {
            log.warn("Skipping job offer due to missing externalId or sourcePlatform: {}", jobOffer.getTitle());
            return Mono.empty(); // No se puede procesar si falta el ID externo o la plataforma
        }

        // Buscar si la oferta ya existe
        return jobOfferRepository.findBySourcePlatformAndExternalId(jobOffer.getSourcePlatform(), jobOffer.getExternalId())
                .flatMap(existingOffer -> {
                    // Si existe, actualizamos solo algunos campos relevantes
                    log.debug("Updating existing job offer: {}", existingOffer.getTitle());
                    existingOffer.setTitle(jobOffer.getTitle());
                    existingOffer.setCompanyName(jobOffer.getCompanyName());
                    existingOffer.setLocation(jobOffer.getLocation());
                    existingOffer.setSalaryRange(jobOffer.getSalaryRange());
                    existingOffer.setPublicationDate(jobOffer.getPublicationDate());
                    existingOffer.setUrl(jobOffer.getUrl()); // URL podría cambiar
                    existingOffer.setCrawledAt(Instant.now()); // Actualizar timestamp de scraping
                    // No actualizamos la descripción completa si no la hemos vuelto a scrapear a fondo
                    // Opcional: añadir lógica para comparar y actualizar campos si han cambiado.
                    return jobOfferRepository.save(existingOffer);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    // Si no existe, guardamos la nueva oferta
                    log.debug("Saving new job offer: {}", jobOffer.getTitle());
                    return jobOfferRepository.save(jobOffer);
                }))
                .doOnError(e -> log.error("Error saving or updating job offer {}: {}", jobOffer.getTitle(), e.getMessage()));
    }
}