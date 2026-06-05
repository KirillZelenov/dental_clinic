package ru.kafpin.dental_clinic.dao;

import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.config.QueryLoader;
import ru.kafpin.dental_clinic.model.Doctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для доступа к данным о врачах стоматологической клиники.
 * Предоставляет методы для выполнения операций CRUD с информацией о врачах,
 * а также для получения списка имён врачей.
 */
public class DoctorDAO {
    private static final Logger logger = LoggerFactory.getLogger(DoctorDAO.class);

    /**
     * Возвращает список всех врачей.
     *
     * @return список всех врачей {@link Doctor}
     */
    public List<Doctor> getAll() {
        List<Doctor> doctors = new ArrayList<>();
        String query = QueryLoader.get("doctor.find_all");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                doctors.add(mapRowToDoctor(rs));
            }
            logger.debug("Загружено врачей: {}", doctors.size());
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке всех врачей", e);
        }
        return doctors;
    }

    /**
     * Возвращает врача по его идентификатору.
     *
     * @param id идентификатор врача
     * @return объект {@link Doctor} или {@code null}, если врач не найден
     */
    public Doctor getById(Long id) {
        String query = QueryLoader.get("doctor.find_by_id");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Doctor doctor = mapRowToDoctor(rs);
                logger.debug("Врач с id={} найден: {}", id, doctor.getFullName());
                return doctor;
            }
            logger.debug("Врач с id={} не найден", id);
        } catch (SQLException e) {
            logger.error("Ошибка при поиске врача по id={}", id, e);
        }
        return null;
    }

    /**
     * Добавляет нового врача в базу данных.
     *
     * @param doctor объект врача для добавления
     * @return {@code true} если добавление успешно, {@code false} в противном случае
     */
    public boolean insert(Doctor doctor) {
        String query = QueryLoader.get("doctor.insert");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, doctor.getFullName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getWorkSchedule());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    doctor.setDoctorId(generatedKeys.getLong(1));
                    logger.info("Добавлен новый врач: id={}, ФИО={}", doctor.getDoctorId(), doctor.getFullName());
                }
                return true;
            }
            logger.warn("Не удалось добавить врача: {}", doctor.getFullName());
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении врача: {}", doctor.getFullName(), e);
        }
        return false;
    }

    /**
     * Обновляет информацию о существующем враче.
     *
     * @param doctor объект врача с обновлёнными данными
     * @return {@code true} если обновление успешно, {@code false} в противном случае
     */
    public boolean update(Doctor doctor) {
        String query = QueryLoader.get("doctor.update");
        logger.debug("Выполнение запроса: {}, id={}", query, doctor.getDoctorId());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, doctor.getFullName());
            ps.setString(2, doctor.getSpecialization());
            ps.setString(3, doctor.getWorkSchedule());
            ps.setLong(4, doctor.getDoctorId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Обновлён врач: id={}, ФИО={}", doctor.getDoctorId(), doctor.getFullName());
            } else {
                logger.warn("Не удалось обновить врача с id={}", doctor.getDoctorId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении врача: id={}", doctor.getDoctorId(), e);
        }
        return false;
    }

    /**
     * Удаляет врача по идентификатору.
     *
     * @param id идентификатор удаляемого врача
     * @return {@code true} если удаление успешно, {@code false} в противном случае
     */
    public boolean delete(Long id) {
        String query = QueryLoader.get("doctor.delete");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.info("Удалён врач с id={}", id);
            } else {
                logger.warn("Не удалось удалить врача с id={}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении врача: id={}", id, e);
        }
        return false;
    }

    /**
     * Возвращает список всех имён врачей.
     *
     * @return список строк с именами врачей
     */
    public List<String> getAllDoctorNames() {
        List<String> names = new ArrayList<>();
        String query = QueryLoader.get("doctor.find_all_names");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                names.add(rs.getString("full_name"));
            }
            logger.debug("Загружено имён врачей: {}", names.size());
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке имён врачей", e);
        }
        return names;
    }

    /**
     * Преобразует текущую строку ResultSet в объект {@link Doctor}.
     *
     * @param rs ResultSet с данными о враче
     * @return объект Doctor
     * @throws SQLException если возникает ошибка при доступе к данным ResultSet
     */
    private Doctor mapRowToDoctor(ResultSet rs) throws SQLException {
        Doctor doctor = new Doctor();
        doctor.setDoctorId(rs.getLong("doctor_id"));
        doctor.setFullName(rs.getString("full_name"));
        doctor.setSpecialization(rs.getString("specialization"));
        doctor.setWorkSchedule(rs.getString("work_schedule"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) doctor.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) doctor.setUpdatedAt(updatedAt.toLocalDateTime());

        return doctor;
    }
}