package ru.kafpin.dental_clinic.dao;

import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.config.QueryLoader;
import ru.kafpin.dental_clinic.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для доступа к данным об услугах стоматологической клиники.
 * Предоставляет методы для выполнения операций CRUD с информацией об услугах:
 * стоимость, длительность выполнения и другие характеристики.
 */
public class ServiceDAO {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDAO.class);

    /**
     * Возвращает список всех услуг.
     *
     * @return список всех услуг {@link Service}
     */
    public List<Service> getAll() {
        List<Service> services = new ArrayList<>();
        String query = QueryLoader.get("service.find_all");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                services.add(mapRowToService(rs));
            }
            logger.debug("Загружено услуг: {}", services.size());
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке всех услуг", e);
        }
        return services;
    }

    /**
     * Возвращает услугу по её идентификатору.
     *
     * @param id идентификатор услуги
     * @return объект {@link Service} или {@code null}, если услуга не найдена
     */
    public Service getById(Long id) {
        String query = QueryLoader.get("service.find_by_id");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToService(rs);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске услуги по id={}", id, e);
        }
        return null;
    }

    /**
     * Добавляет новую услугу в базу данных.
     *
     * @param service объект услуги для добавления
     * @return {@code true} если добавление успешно, {@code false} в противном случае
     */
    public boolean insert(Service service) {
        String query = QueryLoader.get("service.insert");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, service.getServiceName());
            ps.setBigDecimal(2, service.getCost());
            ps.setInt(3, service.getAvgDurationMinutes());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    service.setServiceId(generatedKeys.getLong(1));
                    logger.info("Добавлена новая услуга: id={}, название={}", service.getServiceId(), service.getServiceName());
                }
                return true;
            }
            logger.warn("Не удалось добавить услугу: {}", service.getServiceName());
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении услуги: {}", service.getServiceName(), e);
        }
        return false;
    }

    /**
     * Обновляет информацию о существующей услуге.
     *
     * @param service объект услуги с обновлёнными данными
     * @return {@code true} если обновление успешно, {@code false} в противном случае
     */
    public boolean update(Service service) {
        String query = QueryLoader.get("service.update");
        logger.debug("Выполнение запроса: {}, id={}", query, service.getServiceId());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, service.getServiceName());
            ps.setBigDecimal(2, service.getCost());
            ps.setInt(3, service.getAvgDurationMinutes());
            ps.setLong(4, service.getServiceId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Обновлена услуга: id={}, название={}", service.getServiceId(), service.getServiceName());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении услуги: id={}", service.getServiceId(), e);
        }
        return false;
    }

    /**
     * Удаляет услугу по идентификатору.
     *
     * @param id идентификатор удаляемой услуги
     * @return {@code true} если удаление успешно, {@code false} в противном случае
     */
    public boolean delete(Long id) {
        String query = QueryLoader.get("service.delete");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.info("Удалена услуга с id={}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении услуги: id={}", id, e);
        }
        return false;
    }

    /**
     * Преобразует текущую строку ResultSet в объект {@link Service}.
     *
     * @param rs ResultSet с данными об услуге
     * @return объект Service
     * @throws SQLException если возникает ошибка при доступе к данным ResultSet
     */
    private Service mapRowToService(ResultSet rs) throws SQLException {
        Service service = new Service();
        service.setServiceId(rs.getLong("service_id"));
        service.setServiceName(rs.getString("service_name"));
        service.setCost(rs.getBigDecimal("cost"));
        service.setAvgDurationMinutes(rs.getInt("avg_duration_minutes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) service.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) service.setUpdatedAt(updatedAt.toLocalDateTime());

        return service;
    }
}