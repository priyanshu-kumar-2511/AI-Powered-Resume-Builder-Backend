package com.airesume.sectionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Main application class for the Section Service.
 * This service manages modular resume components (Personal Info, Experience, Education, etc.)
 * and handles ownership verification via inter-service communication.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@EnableFeignClients
public class SectionServiceApplication {

    /**
     * Entry point for the Section Service.
     * Bootstrap the application with environment variables and starts the context.
     * 
     * @param args command line arguments
     */
	public static void main(String[] args) {
		// Load .env file from the project root
		io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
				.directory("../") // Look at the root folder
				.ignoreIfMissing()
				.load();

		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(SectionServiceApplication.class, args);
	}

	// In SectionServiceApplication.java or a @Configuration class:
	@Bean
	public FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
		FilterRegistrationBean<RequestContextFilter> bean = new FilterRegistrationBean<>(new RequestContextFilter());
		// This makes the request context inheritable by child threads (Feign, async)
		((RequestContextFilter) bean.getFilter()).setThreadContextInheritable(true);
		return bean;
	}

}
