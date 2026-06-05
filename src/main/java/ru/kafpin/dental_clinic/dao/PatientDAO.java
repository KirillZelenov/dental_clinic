package ru.kafpin.dental_clinic.dao;

import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.config.QueryLoader;
import ru.kafpin.dental_clinic.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для доступа к данным о пациентах стоматологической клиники.
 * Предоставляет методы для выполнения операций CRUD с информацией о пациентах,
 * а также для поиска пациентов по ключевым словам.
 */
public class PatientDAO {
    private static final Logger logger = LoggerFactory.getLogger(PatientDAO.class);

    /**
     * Возвращает список всех пациентов.
     *
     * @return список всех пациентов {@link Patient}
     */
    public List<Patient> getAll() {
        List<Patient> patients = new ArrayList<>();
        String query = QueryLoader.get("patient.find_all");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                patients.add(mapRowToPatient(rs));
            }
            logger.debug("Загружено пациентов: {}", patients.size());
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке всех пациентов", e);
        }
        return patients;
    }

    /**
     * Возвращает пациента по его идентификатору.
     *
     * @param id идентификатор пациента
     * @return объект {@link Patient} или {@code null}, если пациент не найден
     */
    public Patient getById(Long id) {
        String query = QueryLoader.get("patient.find_by_id");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Patient patient = mapRowToPatient(rs);
                logger.debug("Пациент с id={} найден: {}", id, patient.getFullName());
                return patient;
            }
            logger.debug("Пациент с id={} не найден", id);
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пациента по id={}", id, e);
        }
        return null;
    }

    /**
     * Добавляет нового пациента в базу данных.
     *
     * @param patient объект пациента для добавления
     * @return {@code true} если добавление успешно, {@code false} в противном случае
     */
    public boolean insert(Patient patient) {
        String query = QueryLoader.get("patient.insert");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, patient.getFullName());
            ps.setDate(2, Date.valueOf(patient.getBirthDate()));
            ps.setString(3, patient.getPhone());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getInsurancePolicy());
            ps.setString(6, patient.getAllergies());
            ps.setString(7, patient.getContraindications());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    patient.setPatientId(generatedKeys.getLong(1));
                    logger.info("Добавлен новый пациент: id={}, ФИО={}", patient.getPatientId(), patient.getFullName());
                }
                return true;
            }
            logger.warn("Не удалось добавить пациента: {}", patient.getFullName());
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении пациента: {}", patient.getFullName(), e);
        }
        return false;
    }

    /**
     * Обновляет информацию о существующем пациенте.
     *
     * @param patient объект пациента с обновлёнными данными
     * @return {@code true} если обновление успешно, {@code false} в противном случае
     */
    public boolean update(Patient patient) {
        String query = QueryLoader.get("patient.update");
        logger.debug("Выполнение запроса: {}, id={}", query, patient.getPatientId());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, patient.getFullName());
            ps.setDate(2, Date.valueOf(patient.getBirthDate()));
            ps.setString(3, patient.getPhone());
            ps.setString(4, patient.getEmail());
            ps.setString(5, patient.getInsurancePolicy());
            ps.setString(6, patient.getAllergies());
            ps.setString(7, patient.getContraindications());
            ps.setLong(8, patient.getPatientId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Обновлён пациент: id={}, ФИО={}", patient.getPatientId(), patient.getFullName());
            } else {
                logger.warn("Не удалось обновить пациента с id={}", patient.getPatientId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении пациента: id={}", patient.getPatientId(), e);
        }
        return false;
    }

    /**
     * Удаляет пациента по идентификатору.
     *
     * @param id идентификатор удаляемого пациента
     * @return {@code true} если удаление успешно, {@code false} в противном случае
     */
    public boolean delete(Long id) {
        String query = QueryLoader.get("patient.delete");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.info("Удалён пациент с id={}", id);
            } else {
                logger.warn("Не удалось удалить пациента с id={}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении пациента: id={}", id, e);
        }
        return false;
    }

    /**
     * Выполняет поиск пациентов по ключевому слову.
     * Поиск осуществляется по полям: ФИО, телефон и номер полиса.
     *
     * @param keyword ключевое слово для поиска
     * @return список пациентов {@link Patient}, соответствующих критерию поиска
     */
    public List<Patient> search(String keyword) {
        List<Patient> patients = new ArrayList<>();
        String query = QueryLoader.get("patient.search");
        logger.debug("Выполнение запроса: {}, keyword={}", query, keyword);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            String searchPattern = "%" + keyword + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                patients.add(mapRowToPatient(rs));
            }
            logger.debug("Найдено пациентов по запросу '{}': {}", keyword, patients.size());
        } catch (SQLException e) {
            logger.error("Ошибка при поиске пациентов: keyword={}", keyword, e);
        }
        return patients;
    }

    /**
     * Преобразует текущую строку ResultSet в объект {@link Patient}.
     *
     * @param rs ResultSet с данными о пациенте
     * @return объект Patient
     * @throws SQLException если возникает ошибка при доступе к данным ResultSet
     */
    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientId(rs.getLong("patient_id"));
        patient.setFullName(rs.getString("full_name"));
        patient.setBirthDate(rs.getDate("birth_date").toLocalDate());
        patient.setPhone(rs.getString("phone"));
        patient.setEmail(rs.getString("email"));
        patient.setInsurancePolicy(rs.getString("insurance_policy"));
        patient.setAllergies(rs.getString("allergies"));
        patient.setContraindications(rs.getString("contraindications"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) patient.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) patient.setUpdatedAt(updatedAt.toLocalDateTime());

        return patient;
    }
}