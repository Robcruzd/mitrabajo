package com.talentocolombia.gateway.job.config;

import com.talentocolombia.orchestration.search.grpc.OrchestrationSearchServiceGrpc;
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
    public ManagedChannel orchestrationChannel() {
        return ManagedChannelBuilder
                .forAddress(grpcHost, 9091)
                .usePlaintext()
                .build();
    }

    @Bean
    public OrchestrationSearchServiceGrpc
            .OrchestrationSearchServiceBlockingStub orchestrationStub(
            ManagedChannel channel) {

        return OrchestrationSearchServiceGrpc
                .newBlockingStub(channel);
    }
}

