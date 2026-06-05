package ru.kafpin.dental_clinic.dao;

import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.config.QueryLoader;
import ru.kafpin.dental_clinic.dto.ScheduleItemDTO;
import ru.kafpin.dental_clinic.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) для работы с сущностью {@link Appointment} (записи на приём).
 * <p>
 * Предоставляет методы для выполнения CRUD операций с записями на приём в базе данных,
 * а также дополнительные методы для проверки доступности врачей, получения расписания
 * и управления статусами записей.
 * </p>
 *
 * @see Appointment
 * @see ScheduleItemDTO
 * @see DatabaseConfig
 * @see QueryLoader
 */
public class AppointmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentDAO.class);

    /**
     * Возвращает список всех записей на приём из базы данных.
     *
     * @return список всех записей {@link Appointment}, или пустой список, если записи отсутствуют
     *         или произошла ошибка
     * @see #mapRowToAppointment(ResultSet)
     */
    public List<Appointment> getAll() {
        List<Appointment> appointments = new ArrayList<>();
        String query = QueryLoader.get("appointment.find_all");

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                appointments.add(mapRowToAppointment(rs));
            }
            logger.debug("Загружено записей: {}", appointments.size());
        } catch (SQLException e) {
            logger.error("Ошибка при загрузке всех записей", e);
        }
        return appointments;
    }

    /**
     * Находит запись на приём по её уникальному идентификатору.
     *
     * @param id уникальный идентификатор записи (appointment_id)
     * @return объект {@link Appointment} с указанным ID, или {@code null}, если запись не найдена
     *         или произошла ошибка
     */
    public Appointment getById(Long id) {
        String query = QueryLoader.get("appointment.find_by_id");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapRowToAppointment(rs);
            }
        } catch (SQLException e) {
            logger.error("Ошибка при поиске записи по id={}", id, e);
        }
        return null;
    }

    /**
     * Добавляет новую запись на приём в базу данных.
     * <p>
     * После успешной вставки, сгенерированный базой данных идентификатор
     * устанавливается в переданный объект {@link Appointment}.
     * </p>
     *
     * @param appointment объект {@link Appointment} для вставки (ID будет установлен автоматически)
     * @return {@code true}, если запись успешно добавлена; {@code false} в противном случае
     * @see Appointment#setAppointmentId(Long)
     */
    public boolean insert(Appointment appointment) {
        String query = QueryLoader.get("appointment.insert");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, appointment.getPatientId());
            ps.setLong(2, appointment.getDoctorId());
            ps.setLong(3, appointment.getServiceId());
            ps.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(6, appointment.getStatus());
            ps.setString(7, appointment.getReminderSent());

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = ps.getGeneratedKeys();
                if (generatedKeys.next()) {
                    appointment.setAppointmentId(generatedKeys.getLong(1));
                    logger.info("Создана новая запись: id={}, дата={}, время={}",
                            appointment.getAppointmentId(), appointment.getAppointmentDate(), appointment.getAppointmentTime());
                }
                return true;
            }
            logger.warn("Не удалось создать запись");
        } catch (SQLException e) {
            logger.error("Ошибка при создании записи", e);
        }
        return false;
    }

    /**
     * Обновляет существующую запись на приём в базе данных.
     *
     * @param appointment объект {@link Appointment} с обновлёнными данными (должен содержать корректный ID)
     * @return {@code true}, если запись успешно обновлена; {@code false} в противном случае
     */
    public boolean update(Appointment appointment) {
        String query = QueryLoader.get("appointment.update");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, appointment.getPatientId());
            ps.setLong(2, appointment.getDoctorId());
            ps.setLong(3, appointment.getServiceId());
            ps.setDate(4, Date.valueOf(appointment.getAppointmentDate()));
            ps.setTime(5, Time.valueOf(appointment.getAppointmentTime()));
            ps.setString(6, appointment.getStatus());
            ps.setString(7, appointment.getReminderSent());
            ps.setLong(8, appointment.getAppointmentId());

            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Обновлена запись: id={}", appointment.getAppointmentId());
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении записи: id={}", appointment.getAppointmentId(), e);
        }
        return false;
    }

    /**
     * Удаляет запись на приём из базы данных по её идентификатору.
     *
     * @param id уникальный идентификатор записи для удаления
     * @return {@code true}, если запись успешно удалена; {@code false} в противном случае
     */
    public boolean delete(Long id) {
        String query = QueryLoader.get("appointment.delete");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, id);
            boolean deleted = ps.executeUpdate() > 0;
            if (deleted) {
                logger.info("Удалена запись: id={}", id);
            }
            return deleted;
        } catch (SQLException e) {
            logger.error("Ошибка при удалении записи: id={}", id, e);
        }
        return false;
    }

    /**
     * Проверяет, доступен ли врач для записи в указанный временной интервал.
     * <p>
     * Проверка учитывает существующие записи на приём, которые пересекаются
     * с заданным временным интервалом с учётом длительности услуги.
     * </p>
     *
     * @param doctorId идентификатор врача
     * @param date дата приёма
     * @param startTime планируемое время начала приёма
     * @param endTime планируемое время окончания приёма
     * @return {@code true}, если врач доступен в указанный интервал; {@code false} в противном случае
     */
    public boolean isDoctorAvailableWithDuration(Long doctorId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        String query = QueryLoader.get("appointment.check_availability");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(endTime));
            ps.setTime(4, Time.valueOf(startTime));
            ps.setTime(5, Time.valueOf(startTime));
            ps.setTime(6, Time.valueOf(endTime));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean available = rs.getInt(1) == 0;
                logger.debug("Врач доступен: {}", available);
                return available;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при проверке доступности врача", e);
        }
        return false;
    }

    /**
     * Возвращает список занятых временных слотов для указанного врача на определённую дату.
     * <p>
     * Каждый слот представлен массивом из двух строк: время начала и время окончания.
     * </p>
     *
     * @param doctorId идентификатор врача
     * @param date дата для проверки
     * @return список массивов {@code String[2]}, где элемент [0] - время начала, [1] - время окончания,
     *         или пустой список, если занятых слотов нет или произошла ошибка
     */
    public List<String[]> getBusySlots(Long doctorId, LocalDate date) {
        List<String[]> busySlots = new ArrayList<>();
        String query = QueryLoader.get("appointment.find_busy_slots");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, doctorId);
            ps.setDate(2, Date.valueOf(date));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LocalTime start = rs.getTime("appointment_time").toLocalTime();
                int duration = rs.getInt("avg_duration_minutes");
                LocalTime end = start.plusMinutes(duration);
                busySlots.add(new String[]{start.toString(), end.toString()});
            }
            logger.debug("Найдено занятых слотов: {}", busySlots.size());
        } catch (SQLException e) {
            logger.error("Ошибка при получении занятых слотов", e);
        }
        return busySlots;
    }

    /**
     * Обновляет статус записи на приём.
     *
     * @param appointmentId идентификатор записи
     * @param status новый статус записи (например, "Запланирован", "Завершён", "Отменён")
     * @return {@code true}, если статус успешно обновлён; {@code false} в противном случае
     */
    public boolean updateStatus(Long appointmentId, String status) {
        String query = QueryLoader.get("appointment.update_status");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, status);
            ps.setLong(2, appointmentId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Статус записи {} обновлён на '{}'", appointmentId, status);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при обновлении статуса записи: id={}", appointmentId, e);
        }
        return false;
    }

    /**
     * Отмечает, что напоминание о записи было отправлено пациенту.
     *
     * @param appointmentId идентификатор записи
     * @return {@code true}, если отметка успешно установлена; {@code false} в противном случае
     */
    public boolean markReminderSent(Long appointmentId) {
        String query = QueryLoader.get("appointment.update_reminder");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setLong(1, appointmentId);
            boolean updated = ps.executeUpdate() > 0;
            if (updated) {
                logger.info("Отправлено напоминание для записи: id={}", appointmentId);
            }
            return updated;
        } catch (SQLException e) {
            logger.error("Ошибка при отметке отправки напоминания: id={}", appointmentId, e);
        }
        return false;
    }

    /**
     * Получает расписание приёмов на указанную дату с возможностью фильтрации по врачу.
     * <p>
     * Поддерживаются значения фильтра "Все врачи", "All doctors", "Alle Ärzte" для отключения фильтрации.
     * </p>
     *
     * @param date дата, для которой запрашивается расписание
     * @param doctorName имя врача для фильтрации, или {@code null} / пустая строка / специальные значения
     *                  ("Все врачи", "All doctors", "Alle Ärzte") для получения расписания всех врачей
     * @return список объектов {@link ScheduleItemDTO}, представляющих записи в расписании,
     *         или пустой список, если записи отсутствуют или произошла ошибка
     * @see ScheduleItemDTO
     */
    public List<ScheduleItemDTO> getSchedule(LocalDate date, String doctorName) {
        List<ScheduleItemDTO> schedule = new ArrayList<>();
        String query = QueryLoader.get("appointment.get_schedule");

        if (doctorName != null && !doctorName.isEmpty() && !doctorName.equals("Все врачи")
                && !doctorName.equals("All doctors") && !doctorName.equals("Alle Ärzte")) {
            query += " AND d.full_name = ? ";
        }
        query += " ORDER BY a.appointment_time";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(date));
            if (doctorName != null && !doctorName.isEmpty() && !doctorName.equals("Все врачи")
                    && !doctorName.equals("All doctors") && !doctorName.equals("Alle Ärzte")) {
                ps.setString(2, doctorName);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ScheduleItemDTO item = new ScheduleItemDTO();
                item.setAppointmentId(rs.getLong("appointment_id"));
                item.setPatientId(rs.getLong("patient_id"));

                Time time = rs.getTime("appointment_time");
                int duration = rs.getInt("avg_duration_minutes");
                LocalTime startTime = time.toLocalTime();
                LocalTime endTime = startTime.plusMinutes(duration);
                item.setAppointmentTime(startTime.toString() + "-" + endTime.toString());

                item.setStatus(rs.getString("status"));
                String reminderSent = rs.getString("reminder_sent");
                item.setReminderSent("Да".equals(reminderSent));
                item.setDoctorName(rs.getString("doctor_name"));
                item.setPatientName(rs.getString("patient_name"));
                item.setServiceName(rs.getString("service_name"));
                schedule.add(item);
            }
            logger.debug("Загружено записей в расписании: {}", schedule.size());
        } catch (SQLException e) {
            logger.error("Ошибка при получении расписания", e);
        }
        return schedule;
    }

    /**
     * Возвращает список записей на приём на указанную дату со статусом "Запланирован".
     *
     * @param date дата для поиска записей
     * @return список объектов {@link Appointment} на указанную дату со статусом "Запланирован",
     *         или пустой список, если записи отсутствуют или произошла ошибка
     */
    public List<Appointment> getByDate(LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM appointments WHERE appointment_date = ? AND status = 'Запланирован'";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(date));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                appointments.add(mapRowToAppointment(rs));
            }
        } catch (SQLException e) {
            logger.error("Ошибка при получении записей по дате", e);
        }
        return appointments;
    }

    /**
     * Преобразует текущую строку ResultSet в объект {@link Appointment}.
     * <p>
     * Метод извлекает значения всех колонок таблицы appointments и создаёт
     * соответствующий объект Appointment с установленными полями.
     * </p>
     *
     * @param rs ResultSet, указывающий на текущую строку
     * @return объект {@link Appointment}, заполненный данными из ResultSet
     * @throws SQLException если возникает ошибка при доступе к данным ResultSet
     */
    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(rs.getLong("appointment_id"));
        appointment.setPatientId(rs.getLong("patient_id"));
        appointment.setDoctorId(rs.getLong("doctor_id"));
        appointment.setServiceId(rs.getLong("service_id"));
        appointment.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        appointment.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
        appointment.setStatus(rs.getString("status"));
        appointment.setReminderSent(rs.getString("reminder_sent"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) appointment.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) appointment.setUpdatedAt(updatedAt.toLocalDateTime());

        return appointment;
    }
}