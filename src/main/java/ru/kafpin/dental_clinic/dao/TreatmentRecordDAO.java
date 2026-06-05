package ru.kafpin.dental_clinic.dao;

import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.config.QueryLoader;
import ru.kafpin.dental_clinic.model.TreatmentRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для доступа к данным о записях лечения в стоматологической клинике.
 * Предоставляет методы для выполнения операций CRUD с записями о лечении,
 * а также для получения истории лечения по пациенту или приёму.
 */
public class TreatmentRecordDAO {
    private static final Logger logger = LoggerFactory.getLogger(TreatmentRecordDAO.class);

    /**
     * Возвращает список всех записей о лечении.
     *
     * @return список всех записей {@link TreatmentRecord}
     */
    public List<TreatmentRecord> getAll() {
        List<TreatmentRecord> records = new ArrayList<>();
        String query = QueryLoader.get("treatment.find_all");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                records.add(mapRowToTreatmentRecord(rs));
            }
            logger.debug("Загружено записей о лечении: {}", records.size());
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке всех записей о лечении", e);
        }
        return records;
    }

    /**
     * Возвращает запись о лечении по её идентификатору.
     *
     * @param id идентификатор записи о лечении
     * @return объект {@link TreatmentRecord} или {@code null}, если запись не найдена
     */
    public TreatmentRecord getById(Long id) {
        String query = QueryLoader.get("treatment.find_by_id");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToTreatmentRecord(rs);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске записи о лечении по id={}", id, e);
        }
        return null;
    }

    /**
     * Добавляет новую запись о лечении в базу данных.
     *
     * @param record объект записи о лечении для добавления
     * @return {@code true} если добавление успешно, {@code false} в противном случае
     */
    public boolean insert(TreatmentRecord record) {
        String query = QueryLoader.get("treatment.insert");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, record.getAppointmentId());
            ps.setString(2, record.getToothStatus());
            ps.setString(3, record.getPerformedWork());
            ps.setString(4, record.getPrescriptions());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    record.setTreatmentId(generatedKeys.getLong(1));
                    logger.info("Добавлена запись о лечении: id={}, приём={}",
                            record.getTreatmentId(), record.getAppointmentId());
                }
                return true;
            }
            logger.warn("Не удалось добавить запись о лечении для приёма {}", record.getAppointmentId());
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении записи о лечении", e);
        }
        return false;
    }

    /**
     * Обновляет информацию о существующей записи лечения.
     *
     * @param record объект записи о лечении с обновлёнными данными
     * @return {@code true} если обновление успешно, {@code false} в противном случае
     */
    public boolean update(TreatmentRecord record) {
        String query = QueryLoader.get("treatment.update");
        logger.debug("Выполнение запроса: {}, id={}", query, record.getTreatmentId());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, record.getToothStatus());
            ps.setString(2, record.getPerformedWork());
            ps.setString(3, record.getPrescriptions());
            ps.setLong(4, record.getTreatmentId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Обновлена запись о лечении: id={}", record.getTreatmentId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении записи о лечении: id={}", record.getTreatmentId(), e);
        }
        return false;
    }

    /**
     * Удаляет запись о лечении по идентификатору.
     *
     * @param id идентификатор удаляемой записи
     * @return {@code true} если удаление успешно, {@code false} в противном случае
     */
    public boolean delete(Long id) {
        String query = QueryLoader.get("treatment.delete");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.info("Удалена запись о лечении с id={}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении записи о лечении: id={}", id, e);
        }
        return false;
    }

    /**
     * Возвращает список записей о лечении для указанного пациента.
     *
     * @param patientId идентификатор пациента
     * @return список записей о лечении {@link TreatmentRecord} пациента
     */
    public List<TreatmentRecord> getByPatientId(Long patientId) {
        List<TreatmentRecord> records = new ArrayList<>();
        String query = QueryLoader.get("treatment.find_by_patient");
        logger.debug("Получение истории лечения пациента: patientId={}", patientId);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                records.add(mapRowToTreatmentRecord(rs));
            }
            logger.debug("Найдено записей о лечении для пациента {}: {}", patientId, records.size());
        } catch (SQLException e) {
            logger.error("Ошибка при получении истории лечения пациента: id={}", patientId, e);
        }
        return records;
    }

    /**
     * Возвращает запись о лечении по идентификатору приёма.
     *
     * @param appointmentId идентификатор приёма
     * @return объект {@link TreatmentRecord} или {@code null}, если запись не найдена
     */
    public TreatmentRecord getByAppointmentId(Long appointmentId) {
        String query = QueryLoader.get("treatment.find_by_appointment");
        logger.debug("Поиск лечения по приёму: appointmentId={}", appointmentId);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, appointmentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToTreatmentRecord(rs);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске лечения по приёму: id={}", appointmentId, e);
        }
        return null;
    }

    /**
     * Преобразует текущую строку ResultSet в объект {@link TreatmentRecord}.
     *
     * @param rs ResultSet с данными о записи лечения
     * @return объект TreatmentRecord
     * @throws SQLException если возникает ошибка при доступе к данным ResultSet
     */
    private TreatmentRecord mapRowToTreatmentRecord(ResultSet rs) throws SQLException {
        TreatmentRecord record = new TreatmentRecord();
        record.setTreatmentId(rs.getLong("treatment_id"));
        record.setAppointmentId(rs.getLong("appointment_id"));
        record.setToothStatus(rs.getString("tooth_status"));
        record.setPerformedWork(rs.getString("performed_work"));
        record.setPrescriptions(rs.getString("prescriptions"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) record.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) record.setUpdatedAt(updatedAt.toLocalDateTime());

        return record;
    }
}