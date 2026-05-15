package com.airesume.templateservice.bootstrap;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import com.airesume.templateservice.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Restores bundled default templates when the table is unexpectedly empty.
 * This acts as a safety net for stale Docker images or old Flyway history.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateSeedInitializer implements ApplicationRunner {

    private final TemplateRepository templateRepository;
    private final DataSource dataSource;
    private final ResourceLoader resourceLoader;

    @Value("${app.templates.seed-on-empty:true}")
    private boolean seedOnEmpty;

    @Override
    public void run(ApplicationArguments args) {
        if (!seedOnEmpty) {
            log.info("Template empty-table seeding is disabled.");
            return;
        }

        long templateCount = templateRepository.count();
        if (templateCount > 0) {
            log.info("Template table already has {} records. Skipping bootstrap seeding.", templateCount);
            return;
        }

        log.warn("Template table is empty. Replaying bundled SQL seed scripts.");

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setContinueOnError(false);
        populator.addScript(resourceLoader.getResource("classpath:db/migration/V2__Insert_Data.sql"));
        populator.addScript(resourceLoader.getResource("classpath:db/migration/V3__Fix_Modern_Sidebar.sql"));
        populator.addScript(resourceLoader.getResource("classpath:db/migration/V4__Fix_Navy_Template.sql"));
        populator.execute(dataSource);

        log.info("Template bootstrap seeding completed. {} templates are now available.", templateRepository.count());
    }
}
