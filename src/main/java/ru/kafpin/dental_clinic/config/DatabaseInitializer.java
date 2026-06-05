package ru.kafpin.dental_clinic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private static final String ADMIN_USER = "postgres";
    private static final String ADMIN_PASSWORD = "wkola191105";

    public static boolean createUserIfNotExists(String username, String password) {
        try {
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/postgres", ADMIN_USER, ADMIN_PASSWORD)) {

                Statement stmt = conn.createStatement();

                String checkUserQuery = "SELECT 1 FROM pg_user WHERE usename = '" + username + "'";
                ResultSet rs = stmt.executeQuery(checkUserQuery);

                if (rs.next()) {
                    logger.info("User {} already exists", username);
                    return true;
                }

                String createUserQuery = String.format("CREATE USER \"%s\" WITH PASSWORD '%s'", username, password);
                stmt.execute(createUserQuery);
                logger.info("User {} created", username);

                stmt.execute(String.format("GRANT CONNECT ON DATABASE dental_clinic TO \"%s\"", username));

                try (Connection conn2 = DriverManager.getConnection(
                        "jdbc:postgresql://localhost:5432/dental_clinic", ADMIN_USER, ADMIN_PASSWORD)) {
                    Statement stmt2 = conn2.createStatement();
                    stmt2.execute(String.format("GRANT ALL ON SCHEMA public TO \"%s\"", username));
                    stmt2.execute(String.format("GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO \"%s\"", username));
                    stmt2.execute(String.format("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO \"%s\"", username));
                    stmt2.execute(String.format("GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO \"%s\"", username));
                    stmt2.execute(String.format("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE ON SEQUENCES TO \"%s\"", username));
                    logger.info("Permissions granted for user {}", username);
                }

                return true;
            }
        } catch (Exception e) {
            logger.error("Error creating user {}", username, e);
            return false;
        }
    }

    public static boolean checkDatabaseExists() {
        try {
            Class.forName("org.postgresql.Driver");

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/postgres", ADMIN_USER, ADMIN_PASSWORD)) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'dental_clinic'");
                if (rs.next()) {
                    logger.info("Database dental_clinic exists");
                    return true;
                } else {
                    stmt.execute("CREATE DATABASE dental_clinic");
                    logger.info("Database dental_clinic created");
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Error checking/creating database", e);
            return false;
        }
    }
}