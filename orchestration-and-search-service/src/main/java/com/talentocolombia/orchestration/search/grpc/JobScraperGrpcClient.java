package com.talentocolombia.orchestration.search.grpc;

import com.talentocolombia.jobs.scraper.grpc.GetJobOfferByIdRequest;
import com.talentocolombia.jobs.scraper.grpc.JobOffer;
import com.talentocolombia.jobs.scraper.grpc.JobOffersRequest;
import com.talentocolombia.jobs.scraper.grpc.JobOffersResponse;
import com.talentocolombia.jobs.scraper.grpc.JobScraperServiceGrpc;
import com.talentocolombia.jobs.scraper.grpc.TriggerScrapeRequest;
import com.talentocolombia.jobs.scraper.grpc.TriggerScrapeResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import io.grpc.Channel;

import com.talentocolombia.jobs.scraper.grpc.JobScraperServiceGrpc.JobScraperServiceBlockingStub;
import com.talentocolombia.jobs.scraper.grpc.JobScraperServiceGrpc.JobScraperServiceStub;

@Component
public class JobScraperGrpcClient {

    private final JobScraperServiceBlockingStub blockingStub;
    private final JobScraperServiceStub asyncStub;

    @Autowired
    public JobScraperGrpcClient(JobScraperServiceBlockingStub blockingStub, // Inyecta el stub bloqueante
                                JobScraperServiceStub asyncStub) {
        this.blockingStub = blockingStub;
        this.asyncStub = asyncStub;
    }

    public JobOffersResponse getJobOffers(JobOffersRequest request) {
        return blockingStub.getJobOffers(request);
    }

    public JobOffer getJobOfferById(GetJobOfferByIdRequest request) {
        return blockingStub.getJobOfferById(request);
    }

    public TriggerScrapeResponse triggerScrape(TriggerScrapeRequest request) {
        return blockingStub.triggerScrape(request);
    }

    public JobScraperServiceStub getAsyncStub() {
        return asyncStub;
    }
}