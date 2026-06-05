package ru.kafpin.dental_clinic.dao;

import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.config.QueryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);
    private ResourceBundle bundle;

    public ReportDAO() {
        this.bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", Locale.getDefault());
    }

    public ReportDAO(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public int getPatientCount(LocalDate startDate, LocalDate endDate) {
        String query = QueryLoader.get("report.patient_count");
        logger.debug("Подсчёт количества пациентов за период: {} - {}", startDate, endDate);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("count");
                logger.debug("Количество пациентов за период: {}", count);
                return count;
            }
        } catch (SQLException e) {
            logger.error("Ошибка при подсчёте количества пациентов", e);
        }
        return 0;
    }

    public String getRevenueByDoctor(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        String query = QueryLoader.get("report.revenue_by_doctor");
        logger.debug("Формирование отчёта по выручке врачей за период: {} - {}", startDate, endDate);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));

            ResultSet rs = ps.executeQuery();

            String doctorHeader = getString("report.doctor");
            String revenueHeader = getString("report.revenue");

            report.append(String.format("%-35s %20s\n", doctorHeader, revenueHeader));
            report.append("----------------------------------------------------------\n");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                report.append(String.format("%-35s %20.2f\n",
                        rs.getString("full_name"), rs.getDouble("revenue")));
            }

            if (!hasData) {
                report.append(getString("info.no_data_for_period"));
            }

            logger.info("Отчёт по выручке врачей сформирован");
        } catch (SQLException e) {
            logger.error("Ошибка при формировании отчёта по выручке врачей", e);
            return getString("error.report_generation") + ": " + e.getMessage();
        }
        return report.toString();
    }

    public String getRevenueByService(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        String query = QueryLoader.get("report.revenue_by_service");
        logger.debug("Формирование отчёта по выручке услуг за период: {} - {}", startDate, endDate);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));

            ResultSet rs = ps.executeQuery();

            String serviceHeader = getString("report.service");
            String countHeader = getString("report.count");
            String revenueHeader = getString("report.revenue");

            report.append(String.format("%-35s %10s %15s\n", serviceHeader, countHeader, revenueHeader));
            report.append("------------------------------------------------------------\n");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                report.append(String.format("%-35s %10d %15.2f\n",
                        rs.getString("service_name"), rs.getInt("count"), rs.getDouble("revenue")));
            }

            if (!hasData) {
                report.append(getString("info.no_data_for_period"));
            }

            logger.info("Отчёт по выручке услуг сформирован");
        } catch (SQLException e) {
            logger.error("Ошибка при формировании отчёта по выручке услуг", e);
            return getString("error.report_generation") + ": " + e.getMessage();
        }
        return report.toString();
    }

    public String getOccupancyReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        String query = QueryLoader.get("report.occupancy");
        logger.debug("Формирование отчёта по загруженности за период: {} - {}", startDate, endDate);

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));

            ResultSet rs = ps.executeQuery();

            String doctorHeader = getString("report.doctor");
            String totalHeader = getString("report.total");
            String completedHeader = getString("report.completed");
            String occupancyHeader = getString("report.occupancy_percent");

            report.append(String.format("%-30s %15s %15s %15s\n", doctorHeader, totalHeader, completedHeader, occupancyHeader));
            report.append("--------------------------------------------------------------------\n");

            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                int total = rs.getInt("total_appointments");
                int completed = rs.getInt("completed_appointments");
                double occupancy = total > 0 ? (double) completed / total * 100 : 0;

                report.append(String.format("%-30s %15d %15d %14.1f%%\n",
                        rs.getString("full_name"), total, completed, occupancy));
            }

            if (!hasData) {
                report.append(getString("info.no_data_for_period"));
            }

            logger.info("Отчёт по загруженности сформирован");
        } catch (SQLException e) {
            logger.error("Ошибка при формировании отчёта по загруженности", e);
            return getString("error.report_generation") + ": " + e.getMessage();
        }
        return report.toString();
    }

    private String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
}