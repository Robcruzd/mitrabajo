package com.talentocolombia.jobs.scraper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "com.talentocolombia.jobs.scraper.repository") // <-- Añade esta anotación
public class JobScraperServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobScraperServiceApplication.class, args);
	}

}
