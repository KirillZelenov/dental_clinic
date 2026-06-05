package ru.kafpin.dental_clinic.controller;

import ru.kafpin.dental_clinic.dao.ReportDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReportsController extends BaseController implements Initializable {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button patientCountButton;
    @FXML private Button revenueByDoctorButton;
    @FXML private Button revenueByServiceButton;
    @FXML private Button occupancyButton;
    @FXML private TextArea reportArea;
    @FXML private Button exportButton;
    @FXML private Label reportsTitleLabel;
    @FXML private Label periodLabel;
    @FXML private Label fromLabel;
    @FXML private Label toLabel;

    private ReportDAO reportDAO;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportDAO = new ReportDAO();

        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());
        updateTexts();

        patientCountButton.setOnAction(e -> showPatientCount());
        revenueByDoctorButton.setOnAction(e -> showRevenueByDoctor());
        revenueByServiceButton.setOnAction(e -> showRevenueByService());
        occupancyButton.setOnAction(e -> showOccupancy());
        exportButton.setOnAction(e -> exportReport());
    }

    @Override
    public void updateTexts() {
        updateButton(patientCountButton, "button.patient_count");
        updateButton(revenueByDoctorButton, "button.revenue_by_doctor");
        updateButton(revenueByServiceButton, "button.revenue_by_service");
        updateButton(occupancyButton, "button.occupancy");
        updateButton(exportButton, "button.export");

        if (reportsTitleLabel != null) reportsTitleLabel.setText(getString("title.reports_management"));
        if (periodLabel != null) periodLabel.setText(getString("label.report_period"));
        if (fromLabel != null) fromLabel.setText(getString("label.from"));
        if (toLabel != null) toLabel.setText(getString("label.to"));

        if (reportArea != null) {
            reportArea.setPromptText(getString("label.report_here"));
        }

        if (startDatePicker.getEditor() != null) {
            startDatePicker.getEditor().setPromptText(getString("label.start_date"));
        }

        if (endDatePicker.getEditor() != null) {
            endDatePicker.getEditor().setPromptText(getString("label.end_date"));
        }

        if (reportDAO != null) {
            reportDAO.setBundle(bundle);
        }
    }

    private void showPatientCount() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) {
            showAlert(getString("status.error"), getString("error.select_period"));
            return;
        }
        int count = reportDAO.getPatientCount(start, end);
        String message = getString("label.patient_count_period") + " " + formatDate(start) + " - " + formatDate(end) + ": " + count;
        reportArea.setText(message);
    }

    private void showRevenueByDoctor() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) {
            showAlert(getString("status.error"), getString("error.select_period"));
            return;
        }
        String report = reportDAO.getRevenueByDoctor(start, end);
        System.out.println("Revenue by doctor report:\n" + report); // Для отладки
        reportArea.setText(report);
    }

    private void showRevenueByService() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) {
            showAlert(getString("status.error"), getString("error.select_period"));
            return;
        }
        String report = reportDAO.getRevenueByService(start, end);
        System.out.println("Revenue by service report:\n" + report); // Для отладки
        reportArea.setText(report);
    }

    private void showOccupancy() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null) {
            showAlert(getString("status.error"), getString("error.select_period"));
            return;
        }
        String report = reportDAO.getOccupancyReport(start, end);
        System.out.println("Occupancy report:\n" + report); // Для отладки
        reportArea.setText(report);
    }

    private void exportReport() {
        String content = reportArea.getText();
        if (content.isEmpty() || content.equals(getString("label.report_here"))) {
            showAlert(getString("status.error"), getString("error.no_data"));
            return;
        }
        System.out.println("=== " + getString("button.export") + " " + getString("label.report") + " ===");
        System.out.println(content);
        showAlert(getString("status.success"), getString("info.export_to_console"));
    }

    private String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }
}