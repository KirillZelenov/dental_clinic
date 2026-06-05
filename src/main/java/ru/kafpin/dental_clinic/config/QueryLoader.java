package ru.kafpin.dental_clinic.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class QueryLoader {
    private static final Logger logger = LoggerFactory.getLogger(QueryLoader.class);
    private static final Properties sqlProperties = new Properties();

    static {
        try (InputStream input = QueryLoader.class.getResourceAsStream("/ru/kafpin/dental_clinic/sql/sql.properties")) {
            if (input == null) {
                logger.error("Файл sql.properties не найден!");
                throw new RuntimeException("sql.properties not found");
            }
            sqlProperties.load(input);
            logger.info("SQL-запросы загружены успешно");
        } catch (IOException e) {
            logger.error("Ошибка загрузки sql.properties", e);
            throw new RuntimeException("Failed to load sql.properties", e);
        }
    }

    public static String get(String key) {
        String query = sqlProperties.getProperty(key);
        if (query == null) {
            logger.warn("SQL запрос с ключом '{}' не найден", key);
        }
        return query;
    }
}