package com.company.hrms.platform.starter.webflux.dev;

import java.util.concurrent.atomic.AtomicBoolean;
import org.testcontainers.containers.PostgreSQLContainer;

public final class LocalPostgresTestcontainerBootstrap {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static PostgreSQLContainer<?> container;

    private LocalPostgresTestcontainerBootstrap() {
    }

    public static void startIfEnabled() {
        if (!isEnabled()) {
            return;
        }
        if (isDatabaseOverridden()) {
            return;
        }
        if (STARTED.get()) {
            return;
        }

        synchronized (LocalPostgresTestcontainerBootstrap.class) {
            if (STARTED.get()) {
                return;
            }

            String database = read("HRMS_DB_NAME", "hrms");
            String username = read("HRMS_DB_USERNAME", "hrms");
            String password = read("HRMS_DB_PASSWORD", "hrms");

            container = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName(database)
                    .withUsername(username)
                    .withPassword(password);
            container.start();

            String host = container.getHost();
            Integer port = container.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT);

            System.setProperty("HRMS_DB_USERNAME", username);
            System.setProperty("HRMS_DB_PASSWORD", password);
            System.setProperty("HRMS_JDBC_URL", "jdbc:postgresql://" + host + ":" + port + "/" + database);
            System.setProperty("HRMS_R2DBC_URL", "r2dbc:postgresql://" + host + ":" + port + "/" + database);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (container != null && container.isRunning()) {
                    container.stop();
                }
            }));

            STARTED.set(true);
        }
    }

    private static boolean isEnabled() {
        String flag = read("HRMS_TC_POSTGRES_ENABLED", "true");
        return Boolean.parseBoolean(flag);
    }

    private static boolean isDatabaseOverridden() {
        return hasValue("HRMS_JDBC_URL")
                || hasValue("HRMS_R2DBC_URL")
                || hasValue("SPRING_DATASOURCE_URL")
                || hasValue("SPRING_R2DBC_URL");
    }

    private static boolean hasValue(String key) {
        return valueOf(key) != null;
    }

    private static String read(String key, String fallback) {
        String value = valueOf(key);
        return value == null ? fallback : value;
    }

    private static String valueOf(String key) {
        String systemValue = System.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return null;
    }
}
