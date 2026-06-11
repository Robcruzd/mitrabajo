package com.talentocolombia.jobs.scraper.service;

import com.talentocolombia.jobs.scraper.model.JobOffer;
import com.talentocolombia.jobs.scraper.model.JobOffer.Location;
import com.talentocolombia.jobs.scraper.model.JobOffer.SalaryRange;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ComputrabajoScraper implements JobScraper {

    private static final Logger log = LoggerFactory.getLogger(ComputrabajoScraper.class);
    private static final String PLATFORM_NAME = "Computrabajo";
    private static final String BASE_URL = "https://www.computrabajo.com.co/ofertas-de-trabajo/?q=%s&p=%d"; // q=query, p=page

    // Patron para extraer el ID externo de la URL (ej. de /ofertas-de-trabajo/oferta-de-empleo-de-desarrollador-java-en-bogota-dc-E12345678.htm)
    // private static final Pattern EXTERNAL_ID_PATTERN = Pattern.compile("E(\\d+)\\.htm");
    private static final Pattern EXTERNAL_ID_PATTERN = Pattern.compile("-([0-9a-zA-Z]+)#");

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public Flux<JobOffer> scrapeJobs(String query, String location, int maxPages) {
        String encodedQuery = URLEncoder.encode(query + " " + location, StandardCharsets.UTF_8);
        List<Mono<JobOffer>> jobMonos = new ArrayList<>();

        for (int page = 1; page <= maxPages; page++) {
            String url = String.format(BASE_URL, encodedQuery, page);
            log.info("Scraping Computrabajo URL: {}", url);

            // Fetch document in a non-blocking way using Mono.fromCallable and subscribeOn
            Mono<Document> docMono = Mono.fromCallable(() -> {
                try {
                    return Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .timeout(10 * 1000) // 10 seconds timeout
                            .get();
                } catch (IOException e) {
                    log.error("Error fetching URL {}: {}", url, e.getMessage());
                    throw new RuntimeException(e); // Propagate as RuntimeException for Reactor error handling
                }
            }).subscribeOn(Schedulers.boundedElastic()); // Use boundedElastic for blocking I/O (Jsoup.connect)

            // Process the document and extract job offers
            docMono.flatMapMany(this::parseJobListPage)
                    .doOnNext(jobOffer -> jobMonos.add(Mono.just(jobOffer))) // Collect job offers as Monos
                    .onErrorResume(RuntimeException.class, e -> {
                        log.error("Error parsing or fetching page {}: {}", url, e.getMessage());
                        return Flux.empty(); // Continue with next pages even if one fails
                    })
                    .blockLast(); // Block until all items from the current page are processed.
            // This makes the loop sequential, but the parsing within the page is reactive.
            // For full async, you would collect all page Monos and then flatMap them.
        }
        // Combine all collected Monos into a single Flux
        return Flux.merge(jobMonos);
    }

    /**
     * Parsea una página de lista de ofertas de empleo y devuelve un flujo de JobOffer.
     * @param document El documento HTML de la página de lista.
     * @return Flux<JobOffer> un flujo de ofertas de empleo.
     */
    private Flux<JobOffer> parseJobListPage(Document document) {
        Elements jobElements = document.select("article.box_offer"); // Selector CSS para cada oferta individual
        List<JobOffer> parsedOffers = jobElements.stream()
                .map(element -> {
                    try {
                        // Llama al método de parsing, pero sin reactividad
                        return parseJobElementSincrono(element);
                    } catch (Exception e) {
                        log.error("Error parsing a job element: {}", e.getMessage());
                        return null; // En caso de error, retorna null
                    }
                })
                .filter(Objects::nonNull) // Elimina los elementos nulos
                .collect(Collectors.toList());

        // Convierte la lista de resultados en un Flux
        return Flux.fromIterable(parsedOffers);
//        return Flux.fromIterable(jobElements)
//                .flatMap(this::parseJobElement); // flatMap para procesar cada elemento de oferta reactivamente
    }

    private JobOffer parseJobElementSincrono(Element element) {
        JobOffer jobOffer = new JobOffer();
        jobOffer.setSourcePlatform(PLATFORM_NAME);
        jobOffer.setCrawledAt(Instant.now());
        jobOffer.setActive(true);

        // ... toda tu lógica de parsing sin el Mono.fromCallable ...
        // ... solo devuelve 'jobOffer' al final ...
        // ... y retorna 'null' si no se encuentra el enlace.

        Element linkElement = element.selectFirst("h2 a.js-o-link");
        if (linkElement != null) {
            String relativeUrl = linkElement.attr("href");
            String fullUrl = "https://www.computrabajo.com.co" + relativeUrl;
            jobOffer.setUrl(fullUrl);

            Matcher matcher = EXTERNAL_ID_PATTERN.matcher(fullUrl);
            if (matcher.find()) {
                jobOffer.setExternalId(matcher.group(1));
            } else {
                log.warn("Could not extract externalId from URL: {}", fullUrl);
                jobOffer.setExternalId(null); // Or generate a hash if no ID is found
            }
        } else {
            log.warn("Job link element not found for an offer.");
            return null; // Skip this offer if URL is not found
        }

        // Title
        Element titleElement = element.selectFirst("h2.fs18.fwB");
        Optional.ofNullable(titleElement).map(Element::text).ifPresent(jobOffer::setTitle);

        // Company Name
        Element companyElement = element.selectFirst("p.dFlex a");
        Optional.ofNullable(companyElement).map(Element::text).ifPresent(jobOffer::setCompanyName);

        // Location
        Element locationElement = element.selectFirst("p.fs16:nth-of-type(2)");
        if (locationElement != null) {
            String locationText = locationElement.text();
            // Simple parsing for Colombian context (City, Department, Country)
            Location loc = new Location();
            loc.setCountry("Colombia"); // Assuming it's Colombia for Computrabajo.com.co
            if (locationText.contains(",")) {
                String[] parts = locationText.split(",", 2);
                loc.setCity(parts[0].trim());
                loc.setDepartment(parts[1].trim());
            } else {
                loc.setCity(locationText.trim());
                loc.setDepartment(locationText.trim()); // Fallback for simple cases
            }
            jobOffer.setLocation(loc);
        }

        // Publication Date (Computrabajo uses relative dates, e.g., "Hace 3 días")
        Element dateElement = element.selectFirst("p.fs13.fc_aux");
        if (dateElement != null) {
            String dateText = dateElement.text();
            jobOffer.setPublicationDate(parseComputrabajoDate(dateText));
        }

        // Salary Range (Needs more robust parsing for various formats)
        Element salaryElement = element.selectFirst("div.fs13 span.dIB");
        if (salaryElement != null) {
            String salaryText = salaryElement.text();
            SalaryRange salaryRange = parseSalaryRange(salaryText);
            jobOffer.setSalaryRange(salaryRange);
        }

        return jobOffer;
    }

    /**
     * Parsea un elemento HTML individual de oferta de empleo.
     * @param element El elemento HTML que representa una oferta.
     * @return Mono<JobOffer> un Mono que emitirá la oferta de empleo parseada.
     */
    private Mono<JobOffer> parseJobElement(Element element) {
        return Mono.fromCallable(() -> {
                    JobOffer jobOffer = new JobOffer();
                    jobOffer.setSourcePlatform(PLATFORM_NAME);
                    jobOffer.setCrawledAt(Instant.now());
                    jobOffer.setActive(true);

                    // Extract URL and External ID
                    Element linkElement = element.selectFirst("h2 a.js-o-link");
                    if (linkElement != null) {
                        String relativeUrl = linkElement.attr("href");
                        String fullUrl = "https://www.computrabajo.com.co" + relativeUrl;
                        jobOffer.setUrl(fullUrl);

                        Matcher matcher = EXTERNAL_ID_PATTERN.matcher(fullUrl);
                        if (matcher.find()) {
                            jobOffer.setExternalId(matcher.group(1));
                        } else {
                            log.warn("Could not extract externalId from URL: {}", fullUrl);
                            jobOffer.setExternalId(null); // Or generate a hash if no ID is found
                        }
                    } else {
                        log.warn("Job link element not found for an offer.");
                        return null; // Skip this offer if URL is not found
                    }

                    // Title
                    Element titleElement = element.selectFirst("h2.fs18.fwB");
                    Optional.ofNullable(titleElement).map(Element::text).ifPresent(jobOffer::setTitle);

                    // Company Name
                    Element companyElement = element.selectFirst("p.dFlex a");
                    Optional.ofNullable(companyElement).map(Element::text).ifPresent(jobOffer::setCompanyName);

                    // Location
                    Element locationElement = element.selectFirst("p.fs16");
                    if (locationElement != null) {
                        String locationText = locationElement.text();
                        // Simple parsing for Colombian context (City, Department, Country)
                        Location loc = new Location();
                        loc.setCountry("Colombia"); // Assuming it's Colombia for Computrabajo.com.co
                        if (locationText.contains(",")) {
                            String[] parts = locationText.split(",", 2);
                            loc.setCity(parts[0].trim());
                            loc.setDepartment(parts[1].trim());
                        } else {
                            loc.setCity(locationText.trim());
                            loc.setDepartment(locationText.trim()); // Fallback for simple cases
                        }
                        jobOffer.setLocation(loc);
                    }

                    // Publication Date (Computrabajo uses relative dates, e.g., "Hace 3 días")
                    Element dateElement = element.selectFirst("p.fs13.fc_aux");
                    if (dateElement != null) {
                        String dateText = dateElement.text();
                        jobOffer.setPublicationDate(parseComputrabajoDate(dateText));
                    }

                    // Salary Range (Needs more robust parsing for various formats)
                    Element salaryElement = element.selectFirst("div.fs13 span.dIB");
                    if (salaryElement != null) {
                        String salaryText = salaryElement.text();
                        SalaryRange salaryRange = parseSalaryRange(salaryText);
                        jobOffer.setSalaryRange(salaryRange);
                    }

                    // Employment Type / Experience Level (Often in description or specific tags, may need more advanced parsing)
                    // For MVP, we might leave these to be extracted from detailed page or simple heuristics
                    // Or add specific selectors if available on the listing page.
                    // Example:
                    // Element detailsElement = element.selectFirst("div.offer-details-tags");
                    // if (detailsElement != null) {
                    //     String detailsText = detailsElement.text();
                    //     // Basic extraction for example
                    //     if (detailsText.contains("Tiempo completo")) jobOffer.setEmploymentType("Full-time");
                    //     if (detailsText.contains("Contrato")) jobOffer.setEmploymentType("Contract");
                    // }

                    // Description (For MVP, we usually don't scrape the full description from listing page)
                    // It's better to fetch the full description from the detail page if needed,
                    // but for a listing, a snippet might be available, or we leave it empty.
                    // For now, leave it null or try to get a brief.
                    // jobOffer.setDescription(element.selectFirst("p.description").text()); // Example if available

                    // IMPORTANT: For required skills, benefits, full description etc., you usually need to
                    // visit the detailed job page (jobOffer.getUrl()) and parse that.
                    // For the MVP, we are focusing on listing page data.

                    return jobOffer;
                }).doOnError(e -> log.error("Error parsing job element: {}", e.getMessage())) // Log errors for individual elements
                .onErrorReturn(null); // Return null to filter out failed parsing
    }

    /**
     * Parsea fechas relativas de Computrabajo (ej. "Hace 3 días", "Hace 1 hora").
     * Esto es una aproximación y puede no ser 100% preciso.
     * Para fechas muy antiguas, Computrabajo muestra la fecha real.
     */
    private Instant parseComputrabajoDate(String dateText) {
        dateText = dateText.toLowerCase(Locale.ROOT);
        Instant now = Instant.now();

        if (dateText.contains("hace")) {
            Pattern p = Pattern.compile("hace (\\d+) (dia|dias|hora|horas)");
            Matcher m = p.matcher(dateText);
            if (m.find()) {
                int value = Integer.parseInt(m.group(1));
                String unit = m.group(2);
                if (unit.startsWith("dia")) {
                    return now.minusSeconds(value * 24 * 60 * 60);
                } else if (unit.startsWith("hora")) {
                    return now.minusSeconds(value * 60 * 60);
                }
            }
        } else if (dateText.contains("ayer")) {
            return now.minusSeconds(24 * 60 * 60);
        } else if (dateText.contains("hoy")) {
            return now;
        }
        // Fallback for absolute dates (e.g., "15 Jun", "15 de Junio")
        // This part might need to be more robust depending on Computrabajo's formats
        try {
            // Try different date formats that Computrabajo might use
            // Example: "15 de Junio", "15 Jun"
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd 'de' MMMM", new Locale("es", "CO"));
            DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd MMM", new Locale("es", "CO"));
            // ... more formatters as needed
            // For simplicity, this is just a placeholder. Real-world parsing is complex.
            // Best approach for dates is often to look for machine-readable dates in HTML (if available)
            // or to use a robust date parsing library for natural language.
            log.warn("Could not parse date: {}", dateText);
            return null; // Or throw an exception/return a default
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date '{}': {}", dateText, e.getMessage());
            return null;
        }
    }

    /**
     * Parsea el texto del rango salarial.
     * Esta es una implementación básica y puede necesitar ser más robusta
     * para manejar diferentes formatos de salario (ej. "A convenir", "USD 1000", "COP 2M").
     */
    private SalaryRange parseSalaryRange(String salaryText) {
        SalaryRange sr = new SalaryRange();
        sr.setText(salaryText); // Keep the original text

        // Example: "$1.500.000 - $2.000.000 (Mensual)"
        Pattern rangePattern = Pattern.compile("\\$\\s?([\\d.,]+)\\s?-\\s?\\$\\s?([\\d.,]+)");
        Matcher rangeMatcher = rangePattern.matcher(salaryText);

        if (rangeMatcher.find()) {
            try {
                sr.setMin(parseCurrencyValue(rangeMatcher.group(1)));
                sr.setMax(parseCurrencyValue(rangeMatcher.group(2)));
                sr.setCurrency("COP"); // Assuming COP for Computrabajo.com.co
            } catch (NumberFormatException e) {
                log.warn("Failed to parse salary numbers from range: {}", salaryText);
            }
        } else {
            // Handle single value like "$2.500.000"
            Pattern singlePattern = Pattern.compile("\\$\\s?([\\d.,]+)");
            Matcher singleMatcher = singlePattern.matcher(salaryText);
            if (singleMatcher.find()) {
                try {
                    sr.setMin(parseCurrencyValue(singleMatcher.group(1)));
                    sr.setMax(sr.getMin()); // If single value, min and max are the same
                    sr.setCurrency("COP");
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse single salary number: {}", salaryText);
                }
            }
        }

        return sr;
    }

    private Double parseCurrencyValue(String value) throws NumberFormatException {
        // Remove dots for thousands separator and replace comma for decimal separator
        // Example: "1.500.000" -> "1500000" ; "1,500" -> "1.500"
        String cleanedValue = value.replace(".", "").replace(",", ".");
        return Double.parseDouble(cleanedValue);
    }
}