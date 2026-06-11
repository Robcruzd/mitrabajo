package com.talentocolombia.jobs.scraper.service;

import com.talentocolombia.jobs.scraper.model.JobOffer;
import reactor.core.publisher.Flux;

/**
 * Interfaz para definir un scraper de ofertas de empleo.
 * Cada implementación se encargará de una plataforma específica.
 */
public interface JobScraper {

    /**
     * El nombre único de la plataforma que este scraper maneja (ej. "Computrabajo").
     * @return El nombre de la plataforma.
     */
    String getPlatformName();

    /**
     * Realiza el proceso de scraping y devuelve un flujo reactivo de ofertas de empleo.
     * @param query Opcional. Término de búsqueda para el scraping.
     * @param location Opcional. Ubicación para el scraping.
     * @param maxPages Opcional. Número máximo de páginas a scrapear.
     * @return Flux<JobOffer> un flujo de ofertas de empleo encontradas.
     */
    Flux<JobOffer> scrapeJobs(String query, String location, int maxPages);
}