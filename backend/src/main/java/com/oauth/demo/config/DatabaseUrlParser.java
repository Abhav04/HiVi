package com.oauth.demo.config;

import org.springframework.core.env.Environment;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class DatabaseUrlParser {

    private DatabaseUrlParser() {}

    public static record DatabaseCredentials(String jdbcUrl, String username, String password) {}

    public static DatabaseCredentials resolve(Environment env) {
        String databaseUrl = firstNonBlank(
                env.getProperty("DATABASE_URL"),
                env.getProperty("DATABASE_INTERNAL_URL"),
                env.getProperty("DATABASE_EXTERNAL_URL")
        );

        if (databaseUrl != null) {
            return fromDatabaseUrl(databaseUrl);
        }

        String host = firstNonBlank(
                env.getProperty("DB_HOST"),
                env.getProperty("DATABASE_HOST"),
                env.getProperty("POSTGRES_HOST")
        );

        if (host != null) {
            String port = firstNonBlank(env.getProperty("DB_PORT"), env.getProperty("DATABASE_PORT"), "5432");
            String dbName = firstNonBlank(
                    env.getProperty("DB_NAME"),
                    env.getProperty("DATABASE_NAME"),
                    env.getProperty("POSTGRES_DB"),
                    "postgres"
            );
            String username = firstNonBlank(
                    env.getProperty("DB_USERNAME"),
                    env.getProperty("DB_USER"),
                    env.getProperty("DATABASE_USER"),
                    env.getProperty("POSTGRES_USER")
            );
            String password = firstNonBlank(
                    env.getProperty("DB_PASSWORD"),
                    env.getProperty("DATABASE_PASSWORD"),
                    env.getProperty("POSTGRES_PASSWORD")
            );

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=require";
            return new DatabaseCredentials(jdbcUrl, username, password);
        }

        String jdbcUrl = env.getProperty("SPRING_DATASOURCE_URL");
        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            return new DatabaseCredentials(
                    jdbcUrl,
                    env.getProperty("SPRING_DATASOURCE_USERNAME"),
                    env.getProperty("SPRING_DATASOURCE_PASSWORD")
            );
        }

        String profiles = String.join(",", env.getActiveProfiles());
        if (profiles.contains("prod") || "prod".equals(env.getProperty("SPRING_PROFILES_ACTIVE"))) {
            throw new IllegalStateException(
                    "No database configuration found. Set DATABASE_URL in Render "
                            + "(Environment → Add from Database) or set DB_HOST, DB_USERNAME, DB_PASSWORD."
            );
        }

        return new DatabaseCredentials(
                env.getProperty("spring.datasource.url", "jdbc:postgresql://localhost:5432/editorplatform"),
                env.getProperty("spring.datasource.username", "postgres"),
                env.getProperty("spring.datasource.password", "postgres")
        );
    }

    private static DatabaseCredentials fromDatabaseUrl(String databaseUrl) {
        if (databaseUrl.startsWith("jdbc:")) {
            String jdbcUrl = databaseUrl;
            if (!jdbcUrl.contains("sslmode=")) {
                jdbcUrl += jdbcUrl.contains("?") ? "&sslmode=require" : "?sslmode=require";
            }
            return new DatabaseCredentials(jdbcUrl, null, null);
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

            return new DatabaseCredentials(jdbcUrl, username, password);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse DATABASE_URL: " + maskUrl(databaseUrl), e);
        }
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }

    static String maskUrl(String url) {
        if (url == null) return "null";
        return url.replaceAll("://([^:]+):([^@]+)@", "://***:***@");
    }
}
