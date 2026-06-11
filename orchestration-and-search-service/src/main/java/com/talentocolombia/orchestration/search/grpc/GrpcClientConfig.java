// src/main/java/com/talentocolombia/orchestration/search/grpc/GrpcClientConfig.java
package com.talentocolombia.orchestration.search.grpc;

import com.talentocolombia.jobs.scraper.grpc.JobScraperServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {

    @Value("${grpc.host}")
    private String grpcHost;

    @Bean
    public ManagedChannel managedChannel() {
        // Replace "localhost" and 6565 with your gRPC server's host and port
        return ManagedChannelBuilder.forAddress(grpcHost, 9090)
                .usePlaintext()
                .build();
    }

    @Bean
    public JobScraperServiceGrpc.JobScraperServiceBlockingStub jobScraperServiceBlockingStub(ManagedChannel channel) {
        return JobScraperServiceGrpc.newBlockingStub(channel);
    }

    @Bean
    public JobScraperServiceGrpc.JobScraperServiceStub jobScraperServiceStub(ManagedChannel channel) {
        return JobScraperServiceGrpc.newStub(channel);
    }
}