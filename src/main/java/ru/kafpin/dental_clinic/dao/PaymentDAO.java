package ru.kafpin.dental_clinic.dao;

import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.config.QueryLoader;
import ru.kafpin.dental_clinic.dto.DebtDTO;
import ru.kafpin.dental_clinic.model.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для доступа к данным о платежах в стоматологической клинике.
 * Предоставляет методы для выполнения операций CRUD с платежами,
 * а также для получения информации о задолженностях пациентов.
 */
public class PaymentDAO {
    private static final Logger logger = LoggerFactory.getLogger(PaymentDAO.class);

    /**
     * Возвращает список всех платежей.
     *
     * @return список всех платежей {@link Payment}
     */
    public List<Payment> getAll() {
        List<Payment> payments = new ArrayList<>();
        String query = QueryLoader.get("payment.find_all");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                payments.add(mapRowToPayment(rs));
            }
            logger.debug("Загружено платежей: {}", payments.size());
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке всех платежей", e);
        }
        return payments;
    }

    /**
     * Возвращает платёж по его идентификатору.
     *
     * @param id идентификатор платежа
     * @return объект {@link Payment} или {@code null}, если платёж не найден
     */
    public Payment getById(Long id) {
        String query = QueryLoader.get("payment.find_by_id");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToPayment(rs);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске платежа по id={}", id, e);
        }
        return null;
    }

    /**
     * Добавляет новый платёж в базу данных.
     *
     * @param payment объект платежа для добавления
     * @return {@code true} если добавление успешно, {@code false} в противном случае
     */
    public boolean insert(Payment payment) {
        String query = QueryLoader.get("payment.insert");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, payment.getAppointmentId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setDate(3, Date.valueOf(payment.getPaymentDate()));
            ps.setString(4, payment.getPaymentStatus());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    payment.setPaymentId(generatedKeys.getLong(1));
                    logger.info("Добавлен новый платёж: id={}, сумма={}, приём={}",
                            payment.getPaymentId(), payment.getAmount(), payment.getAppointmentId());
                }
                return true;
            }
            logger.warn("Не удалось добавить платёж для приёма {}", payment.getAppointmentId());
        } catch (SQLException e) {
            logger.error("Ошибка при добавлении платежа", e);
        }
        return false;
    }

    /**
     * Обновляет информацию о существующем платеже.
     *
     * @param payment объект платежа с обновлёнными данными
     * @return {@code true} если обновление успешно, {@code false} в противном случае
     */
    public boolean update(Payment payment) {
        String query = QueryLoader.get("payment.update");
        logger.debug("Выполнение запроса: {}, id={}", query, payment.getPaymentId());

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setBigDecimal(1, payment.getAmount());
            ps.setDate(2, Date.valueOf(payment.getPaymentDate()));
            ps.setString(3, payment.getPaymentStatus());
            ps.setLong(4, payment.getPaymentId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Обновлён платёж: id={}", payment.getPaymentId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении платежа: id={}", payment.getPaymentId(), e);
        }
        return false;
    }

    /**
     * Удаляет платёж по идентификатору.
     *
     * @param id идентификатор удаляемого платежа
     * @return {@code true} если удаление успешно, {@code false} в противном случае
     */
    public boolean delete(Long id) {
        String query = QueryLoader.get("payment.delete");
        logger.debug("Выполнение запроса: {}, id={}", query, id);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.info("Удалён платёж с id={}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении платежа: id={}", id, e);
        }
        return false;
    }

    /**
     * Возвращает список неоплаченных приёмов с информацией о задолженностях.
     *
     * @return список объектов {@link DebtDTO} с информацией о неоплаченных приёмах
     */
    public List<DebtDTO> getUnpaidAppointments() {
        List<DebtDTO> debts = new ArrayList<>();
        String query = QueryLoader.get("payment.find_unpaid");
        logger.debug("Выполнение запроса: {}", query);

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                DebtDTO debt = new DebtDTO();
                debt.setAppointmentId(rs.getLong("appointment_id"));
                debt.setPatientName(rs.getString("patient_name"));
                debt.setAmount(rs.getBigDecimal("amount"));
                String status = rs.getString("payment_status");
                if (status == null || status.trim().isEmpty()) {
                    status = "Не оплачен";
                }
                debt.setPaymentStatus(status);
                debts.add(debt);
            }
            logger.debug("Найдено задолженностей: {}", debts.size());
        } catch (SQLException e) {
            logger.error("Ошибка при получении списка задолженностей", e);
        }
        return debts;
    }

    /**
     * Возвращает общую сумму задолженности указанного пациента.
     *
     * @param patientId идентификатор пациента
     * @return общая сумма долга пациента в виде {@link BigDecimal}
     */
    public BigDecimal getPatientDebt(Long patientId) {
        String query = QueryLoader.get("payment.patient_debt");
        logger.debug("Получение долга пациента: patientId={}", patientId);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, patientId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal debt = rs.getBigDecimal("total_debt");
                logger.debug("Долг пациента id={}: {}", patientId, debt);
                return debt;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении долга пациента: id={}", patientId, e);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Преобразует текущую строку ResultSet в объект {@link Payment}.
     *
     * @param rs ResultSet с данными о платеже
     * @return объект Payment
     * @throws SQLException если возникает ошибка при доступе к данным ResultSet
     */
    private Payment mapRowToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getLong("payment_id"));
        payment.setAppointmentId(rs.getLong("appointment_id"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setPaymentDate(rs.getDate("payment_date").toLocalDate());
        payment.setPaymentStatus(rs.getString("payment_status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) payment.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) payment.setUpdatedAt(updatedAt.toLocalDateTime());

        return payment;
    }
}