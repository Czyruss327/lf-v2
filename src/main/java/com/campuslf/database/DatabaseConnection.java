package com.campuslf.database;

import java.io.InputStream;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
    private static String SQLITE_URL;
    private static DatabaseProvider PROVIDER;
    private static boolean sqliteSchemaInitialized;

    static {
        Map<String, String> dotEnv = loadDotEnv();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
            }

            String configuredProvider = firstPresent(
                    System.getenv("DB_PROVIDER"),
                    dotEnv.get("DB_PROVIDER"),
                    prop.getProperty("db.provider"));

            URL = firstPresent(System.getenv("DB_URL"), dotEnv.get("DB_URL"), prop.getProperty("db.url"));
            USER = firstPresent(System.getenv("DB_USER"), dotEnv.get("DB_USER"), prop.getProperty("db.user"));
            PASSWORD = firstPresent(
                    System.getenv("DB_PASSWORD"),
                    dotEnv.get("DB_PASSWORD"),
                    prop.getProperty("db.password"));

            SQLITE_URL = firstPresent(
                    System.getenv("SQLITE_DB_URL"),
                    dotEnv.get("SQLITE_DB_URL"),
                    prop.getProperty("sqlite.url"),
                    "jdbc:sqlite:LostAndFound.db");

            if ("sqlite".equalsIgnoreCase(configuredProvider) || isBlank(URL)) {
                PROVIDER = DatabaseProvider.SQLITE;
                URL = SQLITE_URL;
                USER = "";
                PASSWORD = "";
            } else {
                PROVIDER = DatabaseProvider.POSTGRESQL;
                if (isBlank(USER) || isBlank(PASSWORD)) {
                    throw new IllegalStateException(
                            "Supabase credentials are missing. Set DB_URL, DB_USER, and DB_PASSWORD or set DB_PROVIDER=sqlite.");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database configuration", e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        Connection conn;
        try {
            conn = PROVIDER == DatabaseProvider.SQLITE
                    ? openSqliteConnection()
                    : DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            if (PROVIDER != DatabaseProvider.POSTGRESQL || !isNetworkFailure(e)) {
                throw e;
            }

            LOGGER.warning("PostgreSQL connection failed; saving to local SQLite database instead.");
            PROVIDER = DatabaseProvider.SQLITE;
            URL = SQLITE_URL;
            USER = "";
            PASSWORD = "";
            conn = openSqliteConnection();
        }
        if (PROVIDER == DatabaseProvider.SQLITE) {
            initializeSqliteSchema(conn);
        }
        return conn;
    }

    public static boolean isSqlite() {
        return PROVIDER == DatabaseProvider.SQLITE;
    }

    public static boolean isPostgreSql() {
        return PROVIDER == DatabaseProvider.POSTGRESQL;
    }

    private static Connection openSqliteConnection() throws SQLException {
        try {
            return DriverManager.getConnection(SQLITE_URL);
        } catch (SQLException e) {
            if (!isMissingSqliteDriver(e)) {
                throw e;
            }
            return openBundledSqliteConnection();
        }
    }

    private static Connection openBundledSqliteConnection() throws SQLException {
        Path m2 = Path.of(System.getProperty("user.home"), ".m2", "repository");
        Path sqliteJar = m2.resolve(Path.of("org", "xerial", "sqlite-jdbc", "3.45.1.0", "sqlite-jdbc-3.45.1.0.jar"));
        Path slf4jApiJar = m2.resolve(Path.of("org", "slf4j", "slf4j-api", "1.7.36", "slf4j-api-1.7.36.jar"));
        Path slf4jSimpleJar = m2.resolve(Path.of("org", "slf4j", "slf4j-simple", "1.7.36", "slf4j-simple-1.7.36.jar"));

        if (!Files.isRegularFile(sqliteJar)) {
            throw new SQLException("SQLite JDBC driver is not available: " + sqliteJar);
        }

        try {
            java.net.URL[] urls = Files.isRegularFile(slf4jApiJar) && Files.isRegularFile(slf4jSimpleJar)
                    ? new java.net.URL[] {
                            sqliteJar.toUri().toURL(),
                            slf4jApiJar.toUri().toURL(),
                            slf4jSimpleJar.toUri().toURL()
                    }
                    : new java.net.URL[] { sqliteJar.toUri().toURL() };
            URLClassLoader loader = new URLClassLoader(urls, DatabaseConnection.class.getClassLoader());
            Driver driver = (Driver) Class.forName("org.sqlite.JDBC", true, loader)
                    .getDeclaredConstructor()
                    .newInstance();
            return driver.connect(SQLITE_URL, new Properties());
        } catch (Exception e) {
            throw new SQLException("Unable to load SQLite JDBC driver", e);
        }
    }

    private static boolean isMissingSqliteDriver(SQLException e) {
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return message.contains("no suitable driver") && message.contains("sqlite");
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
        Path path = resolveEnvPath();

        if (path == null || !Files.isRegularFile(path)) {
            return values;
        }

        try {
            for (String line : Files.readAllLines(path)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#"))
                    continue;
                int separator = trimmed.indexOf('=');
                if (separator <= 0)
                    continue;
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim();
                values.put(key, stripQuotes(value));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read .env file", e);
        }

        return values;
    }

    private static Path resolveEnvPath() {
        try {
            Path codeSource = Path.of(DatabaseConnection.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path appDir = Files.isDirectory(codeSource) ? codeSource : codeSource.getParent();
            Path candidate = appDir.resolve(".env");
            if (Files.isRegularFile(candidate))
                return candidate;
        } catch (Exception ignored) {
        }

        Path cwdCandidate = Path.of(".env");
        if (Files.isRegularFile(cwdCandidate))
            return cwdCandidate;

        return null;
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

    private static boolean isNetworkFailure(SQLException e) {
        for (Throwable current = e; current != null; current = current.getCause()) {
            if (current instanceof java.net.SocketException
                    || current instanceof java.net.SocketTimeoutException
                    || current instanceof java.net.UnknownHostException
                    || current instanceof java.net.ConnectException) {
                return true;
            }
        }

        String state = e.getSQLState();
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        return (state != null && state.startsWith("08"))
                || message.contains("socket")
                || message.contains("connection refused")
                || message.contains("connection timed out")
                || message.contains("network is unreachable");
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static synchronized void initializeSqliteSchema(Connection conn) throws SQLException {
        if (sqliteSchemaInitialized) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS admin (
                        admin_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS category (
                        category_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        category_name TEXT NOT NULL UNIQUE
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS item_reports (
                        report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        admin_id INTEGER,
                        category_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        location_found TEXT NOT NULL,
                        date_reported TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        date_posted DATE NOT NULL DEFAULT CURRENT_DATE,
                        finder_student_id TEXT,
                        finder_contact_num TEXT,
                        image_url TEXT,
                        status TEXT NOT NULL DEFAULT 'LOST',
                        reporter_name TEXT,
                        reporter_id TEXT,
                        reporter_contact_num TEXT,
                        is_anonymous INTEGER NOT NULL DEFAULT 0,
                        report_type TEXT,
                        FOREIGN KEY (admin_id) REFERENCES admin(admin_id),
                        FOREIGN KEY (category_id) REFERENCES category(category_id)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS lost_item_report (
                        lost_report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        admin_id INTEGER,
                        category_id INTEGER NOT NULL,
                        item_name TEXT NOT NULL,
                        description TEXT,
                        image_url TEXT,
                        complainant_name TEXT,
                        complainant_id TEXT,
                        complainant_contact_num TEXT,
                        is_anonymous INTEGER NOT NULL DEFAULT 0,
                        location_found TEXT NOT NULL,
                        date_found DATE,
                        date_reported TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        status TEXT NOT NULL DEFAULT 'LOST',
                        FOREIGN KEY (admin_id) REFERENCES admin(admin_id),
                        FOREIGN KEY (category_id) REFERENCES category(category_id)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS found_item_report (
                        found_report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        admin_id INTEGER,
                        category_id INTEGER NOT NULL,
                        item_name TEXT NOT NULL,
                        description TEXT,
                        image_url TEXT,
                        finder_name TEXT,
                        finder_id TEXT,
                        finder_contact_number TEXT,
                        is_anonymous INTEGER NOT NULL DEFAULT 0,
                        location_found TEXT NOT NULL,
                        date_found DATE,
                        date_reported TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        status TEXT NOT NULL DEFAULT 'FOUND',
                        FOREIGN KEY (admin_id) REFERENCES admin(admin_id),
                        FOREIGN KEY (category_id) REFERENCES category(category_id)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS claim (
                        claim_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        report_id INTEGER,
                        lost_report_id INTEGER,
                        found_report_id INTEGER,
                        admin_id INTEGER,
                        claimant_name TEXT NOT NULL,
                        claimant_student_id TEXT,
                        claimant_id TEXT,
                        claimant_contact TEXT,
                        claimant_contact_number TEXT,
                        course_section TEXT,
                        claimant_signature TEXT,
                        verification_notes TEXT,
                        claim_status TEXT NOT NULL DEFAULT 'Pending',
                        date_claimed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (report_id) REFERENCES item_reports(report_id),
                        FOREIGN KEY (lost_report_id) REFERENCES lost_item_report(lost_report_id),
                        FOREIGN KEY (found_report_id) REFERENCES found_item_report(found_report_id),
                        FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
                    )
                    """);
            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS activity_logs (
                        log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        admin_id INTEGER,
                        activity TEXT NOT NULL,
                        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
                    )
                    """);
            seedAdmins(stmt);
            seedCategories(stmt);
        }
        sqliteSchemaInitialized = true;
    }

    private static void seedAdmins(Statement stmt) throws SQLException {
        stmt.execute("INSERT OR IGNORE INTO admin (admin_id, username, password) VALUES (1, 'admin', 'admin123')");
        stmt.execute(
                "INSERT OR IGNORE INTO admin (admin_id, username, password) VALUES (2, 'pupsrc_admin', 'pup2026')");
        stmt.execute("INSERT OR IGNORE INTO admin (admin_id, username, password) VALUES (3, '123', '123')");
    }

    private static void seedCategories(Statement stmt) throws SQLException {
        stmt.execute("INSERT OR IGNORE INTO category (category_id, category_name) VALUES (1, 'Electronics')");
        stmt.execute("INSERT OR IGNORE INTO category (category_id, category_name) VALUES (2, 'Bags & Wallets')");
        stmt.execute("INSERT OR IGNORE INTO category (category_id, category_name) VALUES (3, 'IDs & Documents')");
        stmt.execute("INSERT OR IGNORE INTO category (category_id, category_name) VALUES (4, 'Clothing')");
        stmt.execute("INSERT OR IGNORE INTO category (category_id, category_name) VALUES (5, 'Others')");
    }

    private enum DatabaseProvider {
        SQLITE,
        POSTGRESQL
    }
}
