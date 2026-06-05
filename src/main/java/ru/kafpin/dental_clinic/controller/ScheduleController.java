package ru.kafpin.dental_clinic.controller;

import ru.kafpin.dental_clinic.dao.AppointmentDAO;
import ru.kafpin.dental_clinic.dao.DoctorDAO;
import ru.kafpin.dental_clinic.dao.PatientDAO;
import ru.kafpin.dental_clinic.dto.ScheduleItemDTO;
import ru.kafpin.dental_clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ScheduleController extends BaseController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    @FXML private DatePicker filterDatePicker;
    @FXML private ComboBox<String> doctorFilterCombo;
    @FXML private Button showScheduleButton;
    @FXML private Button refreshButton;
    @FXML private Button cancelButton;
    @FXML private Button completeButton;
    @FXML private Button sendReminderButton;
    @FXML private TableView<ScheduleItemDTO> scheduleTable;
    @FXML private Label dateLabel;
    @FXML private Label doctorLabel;

    private AppointmentDAO appointmentDAO;
    private DoctorDAO doctorDAO;
    private PatientDAO patientDAO;
    private ObservableList<ScheduleItemDTO> scheduleList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        appointmentDAO = new AppointmentDAO();
        doctorDAO = new DoctorDAO();
        patientDAO = new PatientDAO();

        setupTableColumns();
        filterDatePicker.setValue(LocalDate.now());
        loadDoctorFilter();
        updateTexts();

        showScheduleButton.setOnAction(e -> showSchedule());
        refreshButton.setOnAction(e -> showSchedule());
        cancelButton.setOnAction(e -> cancelAppointment());
        completeButton.setOnAction(e -> completeAppointment());

        if (sendReminderButton != null) {
            sendReminderButton.setOnAction(e -> sendReminder());
        }

        loadScheduleSilently();
    }

    @Override
    public void updateTexts() {
        updateButton(showScheduleButton, "button.show");
        updateButton(refreshButton, "button.refresh");
        updateButton(cancelButton, "button.cancel");
        updateButton(completeButton, "button.complete");
        updateButton(sendReminderButton, "button.send_reminder");

        if (dateLabel != null) dateLabel.setText(getString("label.date"));
        if (doctorLabel != null) doctorLabel.setText(getString("label.doctor"));

        if (filterDatePicker.getEditor() != null) {
            filterDatePicker.getEditor().setPromptText(getString("label.select_date"));
        }

        if (doctorFilterCombo != null && doctorFilterCombo.getItems().size() > 0) {
            String allDoctorsText = getString("label.all_doctors");
            if (!allDoctorsText.equals("label.all_doctors") && doctorFilterCombo.getItems().size() > 0) {
                String currentValue = doctorFilterCombo.getValue();
                doctorFilterCombo.getItems().set(0, allDoctorsText);
                if (currentValue != null && (currentValue.equals("Все врачи") || currentValue.equals("All doctors") || currentValue.equals("Alle Ärzte"))) {
                    doctorFilterCombo.setValue(allDoctorsText);
                }
            }
        }

        updateTableColumnsText();
        scheduleTable.setPlaceholder(new Label(getString("table.schedule.empty_message")));
    }

    private void updateTableColumnsText() {
        if (scheduleTable.getColumns().size() >= 7) {
            scheduleTable.getColumns().get(0).setText(getString("column.id"));
            scheduleTable.getColumns().get(1).setText(getString("column.appointment_time"));
            scheduleTable.getColumns().get(2).setText(getString("column.doctor"));
            scheduleTable.getColumns().get(3).setText(getString("column.patient"));
            scheduleTable.getColumns().get(4).setText(getString("column.service"));
            scheduleTable.getColumns().get(5).setText(getString("column.status"));
            scheduleTable.getColumns().get(6).setText(getString("column.reminder"));
        }
    }

    private void setupTableColumns() {
        scheduleTable.getColumns().clear();

        TableColumn<ScheduleItemDTO, Long> idCol = new TableColumn<>();
        TableColumn<ScheduleItemDTO, String> timeCol = new TableColumn<>();
        TableColumn<ScheduleItemDTO, String> doctorCol = new TableColumn<>();
        TableColumn<ScheduleItemDTO, String> patientCol = new TableColumn<>();
        TableColumn<ScheduleItemDTO, String> serviceCol = new TableColumn<>();
        TableColumn<ScheduleItemDTO, String> statusCol = new TableColumn<>();
        TableColumn<ScheduleItemDTO, Boolean> reminderCol = new TableColumn<>();

        idCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("appointmentTime"));
        doctorCol.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        patientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        serviceCol.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        reminderCol.setCellValueFactory(new PropertyValueFactory<>("reminderSent"));

        idCol.setPrefWidth(60);
        timeCol.setPrefWidth(120);
        doctorCol.setPrefWidth(180);
        patientCol.setPrefWidth(200);
        serviceCol.setPrefWidth(180);
        statusCol.setPrefWidth(100);
        reminderCol.setPrefWidth(120);

        reminderCol.setCellFactory(column -> new TableCell<ScheduleItemDTO, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? getString("label.sent") : getString("label.not_sent"));
                }
            }
        });

        statusCol.setCellFactory(column -> new TableCell<ScheduleItemDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Завершён".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("Отменён".equals(item)) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                }
            }
        });

        scheduleTable.getColumns().addAll(idCol, timeCol, doctorCol, patientCol, serviceCol, statusCol, reminderCol);

        updateTableColumnsText();
        scheduleTable.setPlaceholder(new Label(getString("table.schedule.empty_message")));
    }

    private void loadDoctorFilter() {
        try {
            List<String> doctorNames = doctorDAO.getAllDoctorNames();
            doctorFilterCombo.setItems(FXCollections.observableArrayList(doctorNames));
            doctorFilterCombo.getItems().add(0, getString("label.all_doctors"));
            doctorFilterCombo.setValue(getString("label.all_doctors"));
        } catch (Exception e) {
            logger.error("Error loading doctor filter", e);
            doctorFilterCombo.setItems(FXCollections.observableArrayList(getString("label.all_doctors")));
            doctorFilterCombo.setValue(getString("label.all_doctors"));
        }
    }

    private void loadScheduleSilently() {
        try {
            LocalDate date = filterDatePicker.getValue();
            if (date == null) {
                date = LocalDate.now();
                filterDatePicker.setValue(date);
            }
            String doctorFilter = doctorFilterCombo.getValue();
            if (getString("label.all_doctors").equals(doctorFilter)) doctorFilter = null;

            List<ScheduleItemDTO> schedule = appointmentDAO.getSchedule(date, doctorFilter);
            scheduleList.setAll(schedule);
            scheduleTable.setItems(scheduleList);
        } catch (Exception e) {
            logger.error("Error loading schedule silently", e);
        }
    }

    private void showSchedule() {
        try {
            LocalDate date = filterDatePicker.getValue();
            if (date == null) {
                date = LocalDate.now();
                filterDatePicker.setValue(date);
            }
            String doctorFilter = doctorFilterCombo.getValue();
            if (getString("label.all_doctors").equals(doctorFilter)) doctorFilter = null;

            List<ScheduleItemDTO> schedule = appointmentDAO.getSchedule(date, doctorFilter);
            scheduleList.setAll(schedule);
            scheduleTable.setItems(scheduleList);

            if (scheduleList.isEmpty()) {
                showAlert(getString("status.info"), getString("info.no_records"));
            }
        } catch (Exception e) {
            logger.error("Error showing schedule", e);
            showAlert(getString("status.error"), getString("error.load_schedule") + ": " + e.getMessage());
        }
    }

    private void cancelAppointment() {
        ScheduleItemDTO selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(getString("status.warning"), getString("error.select_appointment"));
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(getString("title.confirm"));
        confirm.setHeaderText(null);
        confirm.setContentText(getString("confirm.cancel_appointment") + " #" + selected.getAppointmentId() + "?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = appointmentDAO.updateStatus(selected.getAppointmentId(), "Отменён");
            if (success) {
                showAlert(getString("status.success"), getString("info.cancelled"));
                showSchedule();
            } else {
                showAlert(getString("status.error"), getString("error.update_failed"));
            }
        }
    }

    private void completeAppointment() {
        ScheduleItemDTO selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(getString("status.warning"), getString("error.select_appointment"));
            return;
        }

        boolean success = appointmentDAO.updateStatus(selected.getAppointmentId(), "Завершён");
        if (success) {
            showAlert(getString("status.success"), getString("info.completed"));
            showSchedule();
        } else {
            showAlert(getString("status.error"), getString("error.update_failed"));
        }
    }

    private void sendReminder() {
        ScheduleItemDTO selected = scheduleTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(getString("status.warning"), getString("error.select_appointment"));
            return;
        }

        String patientPhone = "";
        String patientEmail = "";
        try {
            Patient patient = patientDAO.getById(selected.getPatientId());
            if (patient != null) {
                patientPhone = patient.getPhone() != null ? patient.getPhone() : "";
                patientEmail = patient.getEmail() != null ? patient.getEmail() : "";
            }
        } catch (Exception e) {
            logger.error("Error getting patient info", e);
        }

        Alert reminderAlert = new Alert(Alert.AlertType.INFORMATION);
        reminderAlert.setTitle("📋 Отправка напоминания");
        reminderAlert.setHeaderText("Напоминание для пациента: " + selected.getPatientName());

        String content = "═══════════════════════════════════════════════════\n" +
                "              НАПОМИНАНИЕ О ПРИЁМЕ\n" +
                "═══════════════════════════════════════════════════\n\n" +
                "📅 Дата: " + filterDatePicker.getValue() + "\n" +
                "⏰ Время: " + selected.getAppointmentTime() + "\n" +
                "👨‍⚕️ Врач: " + selected.getDoctorName() + "\n" +
                "💊 Услуга: " + selected.getServiceName() + "\n" +
                "📞 Телефон: " + (patientPhone.isEmpty() ? "не указан" : patientPhone) + "\n" +
                "✉️ Email: " + (patientEmail.isEmpty() ? "не указан" : patientEmail) + "\n\n" +
                "═══════════════════════════════════════════════════\n" +
                "      Напоминание успешно отправлено! ✅\n" +
                "═══════════════════════════════════════════════════";

        reminderAlert.setContentText(content);
        reminderAlert.getDialogPane().setPrefWidth(450);

        boolean updated = appointmentDAO.markReminderSent(selected.getAppointmentId());

        if (updated) {
            reminderAlert.showAndWait();
            showSchedule();
        } else {
            showAlert(getString("status.error"), "Не удалось отметить отправку напоминания в базе данных");
        }
    }
}