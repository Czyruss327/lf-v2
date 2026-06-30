package com.campuslf.database;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        Map<String, String> dotEnv = loadDotEnv();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
            }

            URL = firstPresent(System.getenv("DB_URL"), dotEnv.get("DB_URL"), prop.getProperty("db.url"));
            USER = firstPresent(System.getenv("DB_USER"), dotEnv.get("DB_USER"), prop.getProperty("db.user"));
            PASSWORD = firstPresent(
                    System.getenv("DB_PASSWORD"),
                    dotEnv.get("DB_PASSWORD"),
                    prop.getProperty("db.password"));

            if (isBlank(URL) || isBlank(USER) || isBlank(PASSWORD)) {
                throw new IllegalStateException("Database credentials are missing. Set DB_URL, DB_USER, and DB_PASSWORD.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to close database resource", e);
                }
            }
        }
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        Path path = Path.of(".env");

        if (!Files.isRegularFile(path)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }

                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }

                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                values.put(key, stripQuotes(value));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read .env file", e);
        }

        return values;
    }

    private static String firstPresent(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}