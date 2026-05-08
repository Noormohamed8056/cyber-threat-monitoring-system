package com.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppBeansConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public CommandLineRunner schemaCompatibilityRunner(JdbcTemplate jdbcTemplate) {
        return args -> {
            applySafeAlter(
                    jdbcTemplate,
                    "ALTER TABLE incident_reports MODIFY COLUMN risk_level VARCHAR(20) NULL"
            );
            applySafeAlter(
                    jdbcTemplate,
                    "ALTER TABLE incident_reports MODIFY COLUMN incident_type VARCHAR(40) NULL"
            );
            applySafeAlter(
                    jdbcTemplate,
                    "ALTER TABLE incident_reports MODIFY COLUMN status VARCHAR(30) NULL"
            );
            applySafeAlter(
                    jdbcTemplate,
                    "ALTER TABLE incident_reports MODIFY COLUMN alert_status VARCHAR(30) NULL"
            );
            applySafeAlter(
                    jdbcTemplate,
                    "ALTER TABLE incident_reports MODIFY COLUMN verification_status VARCHAR(40) NULL"
            );
            applySafeAlter(
                    jdbcTemplate,
                    "ALTER TABLE incident_reports MODIFY COLUMN admin_decision VARCHAR(30) NULL"
            );
            applySafeAlter(
                    jdbcTemplate,
                    "ALTER TABLE threat_history MODIFY COLUMN risk_level_at_time VARCHAR(20) NULL"
            );
        };
    }

    private void applySafeAlter(JdbcTemplate jdbcTemplate, String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
            // Keep startup resilient if table/column does not exist yet.
        }
    }
}
