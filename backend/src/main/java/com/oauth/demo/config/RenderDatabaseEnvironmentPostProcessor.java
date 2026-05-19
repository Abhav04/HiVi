package com.oauth.demo.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts Render's DATABASE_URL (postgresql://user:pass@host:port/db)
 * into spring.datasource.* properties before the context starts.
 */
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty("DATABASE_URL");
        String profiles = environment.getProperty("SPRING_PROFILES_ACTIVE", "");
        boolean prod = profiles.contains("prod");

        if (databaseUrl == null || databaseUrl.isBlank()) {
            String jdbcUrl = environment.getProperty("SPRING_DATASOURCE_URL", "");
            if (prod && jdbcUrl.isBlank()) {
                throw new IllegalStateException(
                    "Production requires DATABASE_URL. In Render: create a PostgreSQL database, "
                        + "then link it to this web service (Environment → Add from Database)."
                );
            }
            return;
        }

        if (!databaseUrl.startsWith("postgres")) {
            return;
        }

        try {
            String normalized = databaseUrl.replaceFirst("^postgres://", "postgresql://");
            URI uri = new URI(normalized);

            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String[] parts = uri.getUserInfo().split(":", 2);
                username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                if (parts.length > 1) {
                    password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                }
            }

            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = (uri.getPath() == null || uri.getPath().isBlank()) ? "/postgres" : uri.getPath();

            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + path;
            if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                jdbcUrl += "?" + uri.getQuery();
            } else {
                jdbcUrl += "?sslmode=require";
            }

            Map<String, Object> props = new HashMap<>();
            props.put("spring.datasource.url", jdbcUrl);
            if (username != null) {
                props.put("spring.datasource.username", username);
            }
            if (password != null) {
                props.put("spring.datasource.password", password);
            }

            environment.getPropertySources().addFirst(new MapPropertySource("renderDatabase", props));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse DATABASE_URL for Render PostgreSQL", e);
        }
    }
}
