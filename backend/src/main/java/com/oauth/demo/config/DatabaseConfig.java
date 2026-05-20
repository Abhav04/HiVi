package com.oauth.demo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        DatabaseUrlParser.DatabaseCredentials creds = DatabaseUrlParser.resolve(env);

        log.info("Connecting to database: {}", DatabaseUrlParser.maskUrl(creds.jdbcUrl()));
        log.info("DATABASE_URL env present: {}", env.getProperty("DATABASE_URL") != null);
        log.info("Active profiles: {}", String.join(",", env.getActiveProfiles()));

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(creds.jdbcUrl());
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setConnectionTimeout(30000);
        dataSource.setMaximumPoolSize(5);

        String username = creds.username();
        String password = creds.password();
        if (username == null) {
            username = firstNonBlank(env.getProperty("DB_USERNAME"), env.getProperty("spring.datasource.username"));
        }
        if (password == null) {
            password = firstNonBlank(env.getProperty("DB_PASSWORD"), env.getProperty("spring.datasource.password"));
        }
        if (username != null) {
            dataSource.setUsername(username);
        }
        if (password != null) {
            dataSource.setPassword(password);
        }

        if (creds.jdbcUrl().contains("localhost") && env.getProperty("DATABASE_URL") != null) {
            throw new IllegalStateException(
                    "DATABASE_URL is set but resolved to localhost. Check your Render environment variable value."
            );
        }

        return dataSource;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
