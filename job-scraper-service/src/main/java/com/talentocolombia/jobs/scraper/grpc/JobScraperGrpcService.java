package com.talentocolombia.jobs.scraper.grpc;

// import com.talentocolombia.jobs.scraper.model.JobOffer;
import com.talentocolombia.jobs.scraper.repository.JobOfferRepository;
import com.talentocolombia.jobs.scraper.service.ScrapingOrchestratorService;
import io.grpc.stub.StreamObserver;
//import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.grpc.server.service.GrpcService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Importa las clases generadas por Protobuf
import com.talentocolombia.jobs.scraper.grpc.JobScraperServiceGrpc.JobScraperServiceImplBase;
import com.talentocolombia.jobs.scraper.grpc.JobOffer; // Importa el JobOffer de Protobuf para el mapeo
import com.talentocolombia.jobs.scraper.grpc.Location;
import com.talentocolombia.jobs.scraper.grpc.SalaryRange;

import com.google.protobuf.Timestamp; // Para manejar fechas de Protobuf

@GrpcService // Anotación de net.devh para que Spring detecte este servicio gRPC
public class JobScraperGrpcService extends JobScraperServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(JobScraperGrpcService.class);

    private final JobOfferRepository jobOfferRepository;
    private final ScrapingOrchestratorService scrapingOrchestratorService;
    private final ReactiveMongoTemplate reactiveMongoTemplate; // Para consultas más complejas

    @Autowired
    public JobScraperGrpcService(JobOfferRepository jobOfferRepository,
                                 ScrapingOrchestratorService scrapingOrchestratorService,
                                 ReactiveMongoTemplate reactiveMongoTemplate) {
        this.jobOfferRepository = jobOfferRepository;
        this.scrapingOrchestratorService = scrapingOrchestratorService;
        this.reactiveMongoTemplate = reactiveMongoTemplate;
    }

    /**
     * Implementación del método gRPC GetJobOffers.
     * Permite buscar y filtrar ofertas de empleo de forma reactiva.
     */
    @Override
    public void getJobOffers(JobOffersRequest request, StreamObserver<JobOffersResponse> responseObserver) {
        log.info("Received GetJobOffers request: {}", request);

        // Construir la consulta dinámica para MongoDB
        Query query = new Query();
        if (!request.getQuery().isEmpty()) {
            // Utilizar operador $regex para búsqueda de texto parcial en campos relevantes
            String regex = "(?i).*" + request.getQuery() + ".*"; // Case-insensitive
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(regex),
                    Criteria.where("description").regex(regex),
                    Criteria.where("companyName").regex(regex),
                    Criteria.where("requiredSkills").regex(regex)
            ));
        }
        if (!request.getLocation().isEmpty()) {
            String regex = "(?i).*" + request.getLocation() + ".*";
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("location.city").regex(regex),
                    Criteria.where("location.department").regex(regex)
            ));
        }
        if (!request.getRequiredSkillsList().isEmpty()) {
            query.addCriteria(Criteria.where("requiredSkills").all(request.getRequiredSkillsList()));
        }
        if (!request.getEmploymentType().isEmpty()) {
            query.addCriteria(Criteria.where("employmentType").is(request.getEmploymentType()));
        }
        if (!request.getExperienceLevel().isEmpty()) {
            query.addCriteria(Criteria.where("experienceLevel").is(request.getExperienceLevel()));
        }
        if (request.hasMinPublicationDate()) {
            Instant minDate = Instant.ofEpochSecond(request.getMinPublicationDate().getSeconds(), request.getMinPublicationDate().getNanos());
            query.addCriteria(Criteria.where("publicationDate").gte(minDate));
        }

        // Siempre filtrar por ofertas activas
        query.addCriteria(Criteria.where("isActive").is(true));

        // Paginación y Ordenación
        int page = Math.max(0, request.getPage()); // Asegura que la página sea al menos 0
        int size = Math.min(Math.max(1, request.getSize()), 100); // Tamaño entre 1 y 100
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publicationDate"));
        query.with(pageable);

        // Ejecutar la consulta reactiva y construir la respuesta
        Mono<Long> totalResultsMono = reactiveMongoTemplate.count(query, JobOffer.class); // Contar total de resultados
        Flux<com.talentocolombia.jobs.scraper.model.JobOffer> jobOffersFlux = reactiveMongoTemplate.find(query, com.talentocolombia.jobs.scraper.model.JobOffer.class); // Obtener las ofertas de la página

        Mono.zip(totalResultsMono, jobOffersFlux.collectList())
                .map(tuple -> {
                    Long total = tuple.getT1();
                    List<com.talentocolombia.jobs.scraper.model.JobOffer> offers = tuple.getT2();

                    // Mapear la lista de JobOffer del modelo al tipo JobOffer de gRPC
                    List<JobOffer> grpcJobOffers = offers.stream()
                            .map(this::mapToGrpcJobOffer)
                            .collect(Collectors.toList());

                    // Calcular el total de páginas
                    int totalPages = (int) Math.ceil((double) total / size);

                    // Construir la respuesta gRPC
                    return JobOffersResponse.newBuilder()
                            .addAllJobOffers(grpcJobOffers)
                            .setTotalResults(total)
                            .setCurrentPage(page)
                            .setPageSize(size)
                            .setTotalPages(totalPages)
                            .build();
                })
                .subscribe(
                        response -> {
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                            log.info("Sent GetJobOffers response for query: {}", request.getQuery());
                        },
                        error -> {
                            log.error("Error fetching job offers: {}", error.getMessage(), error);
                            responseObserver.onError(io.grpc.Status.INTERNAL
                                    .withDescription("Error fetching job offers: " + error.getMessage())
                                    .asRuntimeException());
                        }
                );
    }

    /**
     * Implementación del método gRPC GetJobOfferById.
     * Recupera una única oferta de empleo por su ID.
     */
    @Override
    public void getJobOfferById(GetJobOfferByIdRequest request, StreamObserver<JobOffer> responseObserver) {
        log.info("Received GetJobOfferById request for ID: {}", request.getId());
        jobOfferRepository.findById(request.getId())
                .map(this::mapToGrpcJobOffer) // Mapear a la clase JobOffer de gRPC
                .subscribe(
                        jobOffer -> {
                            responseObserver.onNext(jobOffer);
                            responseObserver.onCompleted();
                            log.info("Sent GetJobOfferById response for ID: {}", request.getId());
                        },
                        error -> {
                            log.error("Error retrieving job offer by ID {}: {}", request.getId(), error.getMessage(), error);
                            responseObserver.onError(io.grpc.Status.INTERNAL
                                    .withDescription("Error retrieving job offer: " + error.getMessage())
                                    .asRuntimeException());
                        },
                        () -> { // onComplete sin valor (si no se encuentra la oferta)
                            log.warn("Job offer with ID {} not found.", request.getId());
                            responseObserver.onError(io.grpc.Status.NOT_FOUND
                                    .withDescription("Job offer not found for ID: " + request.getId())
                                    .asRuntimeException());
                        }
                );
    }

    /**
     * Implementación del método gRPC TriggerScrape.
     * Permite iniciar un proceso de scraping manualmente.
     */
    @Override
    public void triggerScrape(TriggerScrapeRequest request, StreamObserver<TriggerScrapeResponse> responseObserver) {
        log.info("Received TriggerScrape request for platform: {}", request.getPlatformName());

        // Hardcoded query and location for MVP demo
        String defaultQuery = "desarrollador";
        String defaultLocation = "Bogotá D.C.";
        int defaultMaxPages = 1; // Para el MVP, limita a una página para no sobrecargar el scraping y la BD

        scrapingOrchestratorService.scrapeAndSave(
                        request.getPlatformName(),
                        request.getQuery().isEmpty() ? defaultQuery : request.getQuery(),
                        request.getLocation().isEmpty() ? defaultLocation : request.getLocation(),
                        request.getMaxPages() == 0 ? defaultMaxPages : request.getMaxPages()
                )
                .subscribe(
                        offersCount -> {
                            TriggerScrapeResponse response = TriggerScrapeResponse.newBuilder()
                                    .setSuccess(true)
                                    .setMessage("Scraping initiated successfully. Offers processed: " + offersCount)
                                    .setOffersFound(offersCount)
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                            log.info("TriggerScrape completed for platform {}. Offers processed: {}", request.getPlatformName(), offersCount);
                        },
                        error -> {
                            log.error("Error during TriggerScrape for platform {}: {}", request.getPlatformName(), error.getMessage(), error);
                            responseObserver.onError(io.grpc.Status.INTERNAL
                                    .withDescription("Error during scraping: " + error.getMessage())
                                    .asRuntimeException());
                        }
                );
    }

    // --- Mappers para convertir de nuestro modelo a los mensajes Protobuf ---

    private JobOffer mapToGrpcJobOffer(com.talentocolombia.jobs.scraper.model.JobOffer modelJobOffer) {
        if (modelJobOffer == null) return null;

        JobOffer.Builder builder = JobOffer.newBuilder()
                .setId(modelJobOffer.getId())
                .setSourcePlatform(modelJobOffer.getSourcePlatform())
                .setUrl(modelJobOffer.getUrl())
                .setTitle(modelJobOffer.getTitle())
                .setCompanyName(modelJobOffer.getCompanyName() == null ? "" : modelJobOffer.getCompanyName())
                .setCrawledAt(toProtobufTimestamp(modelJobOffer.getCrawledAt()))
                .setIsActive(modelJobOffer.isActive());

        Optional.ofNullable(modelJobOffer.getExternalId()).ifPresent(builder::setExternalId);
        Optional.ofNullable(modelJobOffer.getCompanyLogoUrl()).ifPresent(builder::setCompanyLogoUrl);
        Optional.ofNullable(modelJobOffer.getDescription()).ifPresent(builder::setDescription);
        Optional.ofNullable(modelJobOffer.getEmploymentType()).ifPresent(builder::setEmploymentType);
        Optional.ofNullable(modelJobOffer.getExperienceLevel()).ifPresent(builder::setExperienceLevel);
        Optional.ofNullable(modelJobOffer.getContactEmail()).ifPresent(builder::setContactEmail);

        if (modelJobOffer.getLocation() != null) {
            builder.setLocation(Location.newBuilder()
                    .setCity(modelJobOffer.getLocation().getCity() != null ? modelJobOffer.getLocation().getCity() : "")
                    .setDepartment(modelJobOffer.getLocation().getDepartment() != null ? modelJobOffer.getLocation().getDepartment() : "")
                    .setCountry(modelJobOffer.getLocation().getCountry() != null ? modelJobOffer.getLocation().getCountry() : "")
                    .build());
        }

        if (modelJobOffer.getSalaryRange() != null) {
            SalaryRange.Builder salaryBuilder = SalaryRange.newBuilder();
            Optional.ofNullable(modelJobOffer.getSalaryRange().getMin()).ifPresent(salaryBuilder::setMin);
            Optional.ofNullable(modelJobOffer.getSalaryRange().getMax()).ifPresent(salaryBuilder::setMax);
            Optional.ofNullable(modelJobOffer.getSalaryRange().getCurrency()).ifPresent(salaryBuilder::setCurrency);
            Optional.ofNullable(modelJobOffer.getSalaryRange().getText()).ifPresent(salaryBuilder::setText);
            builder.setSalaryRange(salaryBuilder.build());
        }

        Optional.ofNullable(modelJobOffer.getPublicationDate()).map(this::toProtobufTimestamp).ifPresent(builder::setPublicationDate);
        Optional.ofNullable(modelJobOffer.getExpirationDate()).map(this::toProtobufTimestamp).ifPresent(builder::setExpirationDate);
        Optional.ofNullable(modelJobOffer.getApplicationDeadline()).map(this::toProtobufTimestamp).ifPresent(builder::setApplicationDeadline);

        if (modelJobOffer.getRequiredSkills() != null && !modelJobOffer.getRequiredSkills().isEmpty()) {
            builder.addAllRequiredSkills(modelJobOffer.getRequiredSkills());
        }
        if (modelJobOffer.getBenefits() != null && !modelJobOffer.getBenefits().isEmpty()) {
            builder.addAllBenefits(modelJobOffer.getBenefits());
        }
        if (modelJobOffer.getJobFunctions() != null && !modelJobOffer.getJobFunctions().isEmpty()) {
            builder.addAllJobFunctions(modelJobOffer.getJobFunctions());
        }

        return builder.build();
    }

    private Timestamp toProtobufTimestamp(Instant instant) {
        if (instant == null) return null;
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }
}