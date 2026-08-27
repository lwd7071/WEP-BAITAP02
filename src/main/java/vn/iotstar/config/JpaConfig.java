package vn.iotstar.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public final class JpaConfig {
    public static final String PERSISTENCE_UNIT = "jpa-hibernate-sqlserver";
    private static volatile EntityManagerFactory factory;

    private JpaConfig() {
    }

    public static EntityManager getEntityManager() {
        return getFactory().createEntityManager();
    }

    public static EntityManagerFactory getFactory() {
        EntityManagerFactory current = factory;
        if (current == null || !current.isOpen()) {
            synchronized (JpaConfig.class) {
                current = factory;
                if (current == null || !current.isOpen()) {
                    factory = current = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, properties());
                }
            }
        }
        return current;
    }

    static Map<String, Object> properties() {
        Map<String, Object> values = new HashMap<>();
        values.put("jakarta.persistence.jdbc.user", env("DB_USER", "sa"));
        values.put("jakarta.persistence.jdbc.password", env("DB_PASSWORD", ""));
        String customUrl = System.getenv("DB_URL");
        if (customUrl != null && !customUrl.isBlank()) {
            values.put("jakarta.persistence.jdbc.url", customUrl.trim());
        }
        return values;
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    public static synchronized void close() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
        factory = null;
    }
}
