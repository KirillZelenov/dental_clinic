package ru.kafpin.dental_clinic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static String dbUrlBase;
    private static String dbName;
    private static Connection connection;
    private static String currentUser;
    private static String currentPassword;

    static {
        String configPath = "/ru/kafpin/dental_clinic/config/config.properties";
        logger.info("Loading config from: {}", configPath);

        try (InputStream input = DatabaseConfig.class.getResourceAsStream(configPath)) {
            if (input == null) {
                logger.error("config.properties not found at: {}", configPath);
                dbUrlBase = "jdbc:postgresql://localhost:5432/";
                dbName = "dental_clinic";
                logger.warn("Using default values: url={}, db={}", dbUrlBase, dbName);
            } else {
                Properties prop = new Properties();
                prop.load(input);
                dbUrlBase = prop.getProperty("db.url", "jdbc:postgresql://localhost:5432/");
                dbName = prop.getProperty("db.name", "dental_clinic");
                logger.info("Config loaded: url={}, db={}", dbUrlBase, dbName);
            }
        } catch (IOException e) {
            logger.error("Error loading config.properties", e);
            dbUrlBase = "jdbc:postgresql://localhost:5432/";
            dbName = "dental_clinic";
        }

        try {
            Class.forName("org.postgresql.Driver");
            logger.info("PostgreSQL JDBC Driver loaded");
        } catch (ClassNotFoundException e) {
            logger.error("PostgreSQL JDBC Driver not found", e);
        }
    }

    public static void initConnection(String user, String password) throws SQLException {
        currentUser = user;
        currentPassword = password;

        if (connection != null && !connection.isClosed()) {
            closeConnection();
        }

        String fullUrl = dbUrlBase + dbName + "?useUnicode=true&characterEncoding=UTF-8";
        logger.info("Connecting to DB: {}, user: {}", fullUrl, user);

        try {
            connection = DriverManager.getConnection(fullUrl, user, password);
            logger.info("Database connection established successfully");
        } catch (SQLException e) {
            // Логируем оригинальное сообщение, но пользователю покажем понятное
            logger.error("Connection error: {}", e.getMessage());
            throw e;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (currentUser != null && currentPassword != null) {
                logger.info("Reconnecting to database...");
                String fullUrl = dbUrlBase + dbName + "?useUnicode=true&characterEncoding=UTF-8";
                connection = DriverManager.getConnection(fullUrl, currentUser, currentPassword);
                logger.info("Reconnected successfully");
            } else {
                throw new SQLException("Connection not initialized. Call initConnection() first");
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Database connection closed");
            } catch (SQLException e) {
                logger.error("Error closing connection", e);
            }
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}