package ru.kafpin.dental_clinic.controller;

import ru.kafpin.dental_clinic.dao.*;
import ru.kafpin.dental_clinic.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class AppointmentController extends BaseController implements Initializable {

    @FXML private ComboBox<Patient> patientCombo;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private ComboBox<Service> serviceCombo;
    @FXML private DatePicker appointmentDatePicker;
    @FXML private CheckBox reminderSentCheck;
    @FXML private Button saveButton;
    @FXML private Button checkSlotButton;
    @FXML private Button clearButton;
    @FXML private Label durationLabel;
    @FXML private VBox timeSlotsContainer;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableView<Doctor> doctorTable;
    @FXML private TextField patientSearchField;
    @FXML private TextField doctorSearchField;
    @FXML private Label titleLabel;
    @FXML private Label patientSelectionLabel;
    @FXML private Label doctorSelectionLabel;
    @FXML private Label serviceSelectionLabel;
    @FXML private Label appointmentDateLabel;
    @FXML private Label patientListLabel;
    @FXML private Label doctorListLabel;
    @FXML private Label availableTimeLabel;

    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;
    private ServiceDAO serviceDAO;
    private AppointmentDAO appointmentDAO;

    private ObservableList<Patient> allPatients = FXCollections.observableArrayList();
    private ObservableList<Doctor> allDoctors = FXCollections.observableArrayList();

    private List<String> allTimeSlots = Arrays.asList(
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
            "15:00", "15:30", "16:00", "16:30", "17:00"
    );

    private Map<String, Boolean> slotAvailability = new HashMap<>();
    private String selectedTimeSlot = null;
    private List<String[]> busySlotsCache = new ArrayList<>();
    private LocalDate lastCheckedDate = null;
    private Long lastCheckedDoctorId = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            patientDAO = new PatientDAO();
            doctorDAO = new DoctorDAO();
            serviceDAO = new ServiceDAO();
            appointmentDAO = new AppointmentDAO();

            setupPatientTable();
            setupDoctorTable();
            loadPatients();
            loadDoctors();
            loadServices();
            setupTimeSlots();
            updateTexts();

            saveButton.setOnAction(e -> saveAppointment());
            checkSlotButton.setOnAction(e -> checkDoctorAvailability());
            clearButton.setOnAction(e -> clearForm());

            serviceCombo.setOnAction(e -> {
                showServiceDuration();
                updateTimeSlotsDisplay();
            });

            doctorCombo.valueProperty().addListener((obs, old, val) -> updateTimeSlotsDisplay());
            appointmentDatePicker.valueProperty().addListener((obs, old, val) -> updateTimeSlotsDisplay());

            appointmentDatePicker.setValue(LocalDate.now());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(getString("status.error"), e.getMessage());
        }
    }

    @Override
    public void updateTexts() {
        if (titleLabel != null) titleLabel.setText(getString("title.appointment_form"));
        if (patientSelectionLabel != null) patientSelectionLabel.setText(getString("label.patient_selection"));
        if (doctorSelectionLabel != null) doctorSelectionLabel.setText(getString("label.doctor_selection"));
        if (serviceSelectionLabel != null) serviceSelectionLabel.setText(getString("label.service_selection"));
        if (appointmentDateLabel != null) appointmentDateLabel.setText(getString("label.appointment_date"));
        if (patientListLabel != null) patientListLabel.setText(getString("label.patient_list"));
        if (doctorListLabel != null) doctorListLabel.setText(getString("label.doctor_list"));
        if (availableTimeLabel != null) availableTimeLabel.setText(getString("label.available_time"));

        updateButton(saveButton, "button.save");
        updateButton(checkSlotButton, "button.check");
        updateButton(clearButton, "button.clear");

        if (reminderSentCheck != null) {
            reminderSentCheck.setText(getString("checkbox.send_reminder"));
        }

        updateTextFieldPrompt(patientSearchField, "label.search_patient");
        updateTextFieldPrompt(doctorSearchField, "label.search_doctor");

        updateComboBoxPrompt(patientCombo, "label.select_patient");
        updateComboBoxPrompt(doctorCombo, "label.select_doctor");
        updateComboBoxPrompt(serviceCombo, "label.select_service");

        if (durationLabel != null && serviceCombo.getValue() == null) {
            durationLabel.setText(getString("title.select_service_hint"));
        }

        updateTableColumnsText();
        refreshSlotsDisplay();
    }

    private void updateTableColumnsText() {
        if (patientTable.getColumns().size() >= 3) {
            patientTable.getColumns().get(0).setText(getString("column.id"));
            patientTable.getColumns().get(1).setText(getString("column.full_name"));
            patientTable.getColumns().get(2).setText(getString("column.phone"));
        }

        if (doctorTable.getColumns().size() >= 3) {
            doctorTable.getColumns().get(0).setText(getString("column.id"));
            doctorTable.getColumns().get(1).setText(getString("column.full_name"));
            doctorTable.getColumns().get(2).setText(getString("column.specialization"));
        }
    }

    private void setupPatientTable() {
        patientTable.getColumns().clear();

        TableColumn<Patient, Long> idCol = new TableColumn<>();
        TableColumn<Patient, String> nameCol = new TableColumn<>();
        TableColumn<Patient, String> phoneCol = new TableColumn<>();

        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().getPatientId()).asObject());
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        phoneCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPhone()));

        idCol.setPrefWidth(50);
        nameCol.setPrefWidth(200);
        phoneCol.setPrefWidth(120);

        patientTable.getColumns().addAll(idCol, nameCol, phoneCol);

        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) patientCombo.setValue(val);
        });

        patientSearchField.textProperty().addListener((obs, old, val) -> filterPatients(val));
    }

    private void filterPatients(String text) {
        if (text == null || text.isEmpty()) {
            patientTable.setItems(allPatients);
        } else {
            String lower = text.toLowerCase();
            ObservableList<Patient> filtered = FXCollections.observableArrayList();
            for (Patient p : allPatients) {
                if (p.getFullName().toLowerCase().contains(lower) ||
                        p.getPhone().toLowerCase().contains(lower)) {
                    filtered.add(p);
                }
            }
            patientTable.setItems(filtered);
        }
    }

    private void setupDoctorTable() {
        doctorTable.getColumns().clear();

        TableColumn<Doctor, Long> idCol = new TableColumn<>();
        TableColumn<Doctor, String> nameCol = new TableColumn<>();
        TableColumn<Doctor, String> specCol = new TableColumn<>();

        idCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().getDoctorId()).asObject());
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFullName()));
        specCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSpecialization()));

        idCol.setPrefWidth(50);
        nameCol.setPrefWidth(200);
        specCol.setPrefWidth(150);

        doctorTable.getColumns().addAll(idCol, nameCol, specCol);

        doctorTable.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
            if (val != null) doctorCombo.setValue(val);
        });

        doctorSearchField.textProperty().addListener((obs, old, val) -> filterDoctors(val));
    }

    private void filterDoctors(String text) {
        if (text == null || text.isEmpty()) {
            doctorTable.setItems(allDoctors);
        } else {
            String lower = text.toLowerCase();
            ObservableList<Doctor> filtered = FXCollections.observableArrayList();
            for (Doctor d : allDoctors) {
                if (d.getFullName().toLowerCase().contains(lower) ||
                        d.getSpecialization().toLowerCase().contains(lower)) {
                    filtered.add(d);
                }
            }
            doctorTable.setItems(filtered);
        }
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientDAO.getAll();
            allPatients.setAll(patients);
            patientTable.setItems(allPatients);
            patientCombo.setItems(allPatients);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(getString("status.error"), getString("error.load_patients") + ": " + e.getMessage());
        }
    }

    private void loadDoctors() {
        try {
            List<Doctor> doctors = doctorDAO.getAll();
            allDoctors.setAll(doctors);
            doctorTable.setItems(allDoctors);
            doctorCombo.setItems(allDoctors);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(getString("status.error"), getString("error.load_doctors") + ": " + e.getMessage());
        }
    }

    private void loadServices() {
        try {
            List<Service> services = serviceDAO.getAll();
            ObservableList<Service> serviceList = FXCollections.observableArrayList(services);
            serviceCombo.setItems(serviceList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(getString("status.error"), getString("error.load_services") + ": " + e.getMessage());
        }
    }

    private void setupTimeSlots() {
        timeSlotsContainer.getChildren().clear();
        drawTimeSlots();
    }

    private void drawTimeSlots() {
        timeSlotsContainer.getChildren().clear();
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        int col = 0, row = 0;
        for (String timeSlot : allTimeSlots) {
            SlotCell slotCell = new SlotCell(timeSlot);
            final String slot = timeSlot;
            slotCell.setOnMouseClicked(e -> {
                if (slotCell.isAvailable()) {
                    clearSlotSelection();
                    slotCell.setSelected(true);
                    selectedTimeSlot = slot;
                }
            });
            grid.add(slotCell, col, row);
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
        timeSlotsContainer.getChildren().add(grid);
    }

    private void clearSlotSelection() {
        if (timeSlotsContainer.getChildren().isEmpty()) return;
        if (timeSlotsContainer.getChildren().get(0) instanceof GridPane) {
            GridPane grid = (GridPane) timeSlotsContainer.getChildren().get(0);
            for (javafx.scene.Node node : grid.getChildren()) {
                if (node instanceof SlotCell) {
                    ((SlotCell) node).setSelected(false);
                }
            }
        }
    }

    private void updateTimeSlotsDisplay() {
        Doctor selectedDoctor = doctorCombo.getValue();
        LocalDate date = appointmentDatePicker.getValue();
        Service selectedService = serviceCombo.getValue();

        if (selectedDoctor == null || date == null) return;

        if (lastCheckedDate == null || !lastCheckedDate.equals(date) ||
                lastCheckedDoctorId == null || !lastCheckedDoctorId.equals(selectedDoctor.getDoctorId())) {
            busySlotsCache = appointmentDAO.getBusySlots(selectedDoctor.getDoctorId(), date);
            lastCheckedDate = date;
            lastCheckedDoctorId = selectedDoctor.getDoctorId();
        }

        int duration = (selectedService != null) ? selectedService.getAvgDurationMinutes() : 30;
        slotAvailability.clear();

        for (String timeSlot : allTimeSlots) {
            LocalTime startTime = LocalTime.parse(timeSlot);
            LocalTime endTime = startTime.plusMinutes(duration);
            boolean isAvailable = true;

            if (endTime.isAfter(LocalTime.of(20, 0))) {
                isAvailable = false;
            } else {
                for (String[] busy : busySlotsCache) {
                    LocalTime busyStart = LocalTime.parse(busy[0]);
                    LocalTime busyEnd = LocalTime.parse(busy[1]);
                    if (!(endTime.compareTo(busyStart) <= 0 || startTime.compareTo(busyEnd) >= 0)) {
                        isAvailable = false;
                        break;
                    }
                }
            }
            slotAvailability.put(timeSlot, isAvailable);
        }
        refreshSlotsDisplay();
    }

    private void refreshSlotsDisplay() {
        if (timeSlotsContainer.getChildren().isEmpty()) {
            drawTimeSlots();
        }
        if (timeSlotsContainer.getChildren().get(0) instanceof GridPane) {
            GridPane grid = (GridPane) timeSlotsContainer.getChildren().get(0);
            for (javafx.scene.Node node : grid.getChildren()) {
                if (node instanceof SlotCell) {
                    SlotCell slotCell = (SlotCell) node;
                    Boolean available = slotAvailability.get(slotCell.getTimeSlot());
                    if (available != null) {
                        slotCell.setAvailable(available);
                    }
                }
            }
        }
    }

    private class SlotCell extends javafx.scene.layout.HBox {
        private final String timeSlot;
        private boolean available = true;
        private boolean selected = false;
        private final Label timeLabel;
        private final Label statusLabel;

        public SlotCell(String timeSlot) {
            this.timeSlot = timeSlot;
            this.timeLabel = new Label(timeSlot);
            this.statusLabel = new Label();
            setAlignment(Pos.CENTER);
            setSpacing(8);
            setPrefWidth(100);
            setMinHeight(40);
            setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ccc; -fx-padding: 5;");
            getChildren().addAll(timeLabel, statusLabel);
            setOnMouseEntered(e -> setCursor(javafx.scene.Cursor.HAND));
            setOnMouseExited(e -> setCursor(javafx.scene.Cursor.DEFAULT));
        }

        public String getTimeSlot() { return timeSlot; }
        public boolean isAvailable() { return available; }

        public void setAvailable(boolean available) {
            this.available = available;
            updateStyle();
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            updateStyle();
        }

        private void updateStyle() {
            String baseStyle = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #ccc; -fx-padding: 5;";
            if (selected) {
                baseStyle += "-fx-border-width: 2; -fx-border-color: #2196F3; -fx-background-color: #BBDEFB;";
                timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0D47A1;");
                statusLabel.setText("✓");
                statusLabel.setTextFill(Color.GREEN);
            } else if (available) {
                baseStyle += "-fx-background-color: #C8E6C9;";
                timeLabel.setStyle("-fx-text-fill: #1B5E20;");
                statusLabel.setText(getString("status.available"));
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setStyle("-fx-font-size: 10px;");
            } else {
                baseStyle += "-fx-background-color: #FFCDD2;";
                timeLabel.setStyle("-fx-text-fill: #B71C1C;");
                statusLabel.setText(getString("status.busy"));
                statusLabel.setTextFill(Color.RED);
                statusLabel.setStyle("-fx-font-size: 10px;");
            }
            setStyle(baseStyle);
        }
    }

    private void showServiceDuration() {
        Service selected = serviceCombo.getValue();
        if (selected != null && durationLabel != null) {
            durationLabel.setText(getString("label.duration") + ": " + selected.getAvgDurationMinutes() + " " + getString("label.minutes"));
            updateTimeSlotsDisplay();
        } else if (durationLabel != null) {
            durationLabel.setText(getString("title.select_service_hint"));
        }
    }

    private void checkDoctorAvailability() {
        Doctor selectedDoctor = doctorCombo.getValue();
        LocalDate date = appointmentDatePicker.getValue();
        String timeStr = selectedTimeSlot;
        Service selectedService = serviceCombo.getValue();

        if (selectedDoctor == null) {
            showAlert(getString("status.error"), getString("error.select_doctor"));
            return;
        }
        if (date == null) {
            showAlert(getString("status.error"), getString("error.select_date"));
            return;
        }
        if (timeStr == null) {
            showAlert(getString("status.error"), getString("error.select_time"));
            return;
        }
        if (selectedService == null) {
            showAlert(getString("status.error"), getString("error.select_service"));
            return;
        }

        LocalTime startTime = LocalTime.parse(timeStr);
        int duration = selectedService.getAvgDurationMinutes();
        LocalTime endTime = startTime.plusMinutes(duration);

        boolean isAvailable = appointmentDAO.isDoctorAvailableWithDuration(
                selectedDoctor.getDoctorId(), date, startTime, endTime
        );

        if (isAvailable) {
            showAlert(getString("status.success"),
                    getString("info.available") + "\n" +
                            getString("label.appointment_time") + ": " + startTime + " - " + endTime + "\n" +
                            getString("label.duration") + ": " + duration + " " + getString("label.minutes"));
        } else {
            showAlert(getString("status.error"),
                    getString("error.time_unavailable") + "\n" +
                            getString("label.appointment_time") + ": " + startTime + " - " + endTime);
        }
    }

    private void saveAppointment() {
        Patient patient = patientCombo.getValue();
        Doctor doctor = doctorCombo.getValue();
        Service service = serviceCombo.getValue();
        LocalDate date = appointmentDatePicker.getValue();
        String timeStr = selectedTimeSlot;
        boolean reminderSent = reminderSentCheck.isSelected();

        if (patient == null) {
            showAlert(getString("status.error"), getString("error.select_patient"));
            return;
        }
        if (doctor == null) {
            showAlert(getString("status.error"), getString("error.select_doctor"));
            return;
        }
        if (service == null) {
            showAlert(getString("status.error"), getString("error.select_service"));
            return;
        }
        if (date == null) {
            showAlert(getString("status.error"), getString("error.select_date"));
            return;
        }
        if (timeStr == null) {
            showAlert(getString("status.error"), getString("error.select_time"));
            return;
        }

        LocalTime startTime = LocalTime.parse(timeStr);
        int duration = service.getAvgDurationMinutes();
        LocalTime endTime = startTime.plusMinutes(duration);

        if (date.isBefore(LocalDate.now())) {
            showAlert(getString("status.error"), getString("error.past_date"));
            return;
        }

        if (endTime.isAfter(LocalTime.of(20, 0))) {
            showAlert(getString("status.error"), getString("error.time_unavailable"));
            return;
        }

        boolean isAvailable = appointmentDAO.isDoctorAvailableWithDuration(
                doctor.getDoctorId(), date, startTime, endTime
        );

        if (!isAvailable) {
            showAlert(getString("status.error"), getString("error.time_unavailable"));
            return;
        }

        Appointment appointment = new Appointment();
        appointment.setPatientId(patient.getPatientId());
        appointment.setDoctorId(doctor.getDoctorId());
        appointment.setServiceId(service.getServiceId());
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(startTime);
        appointment.setStatus("Запланирован");
        appointment.setReminderSent(reminderSent ? "Да" : "Нет");

        boolean success = appointmentDAO.insert(appointment);
        if (success) {
            String message = getString("info.appointment_created") + "\n" +
                    getString("label.patient") + ": " + patient.getFullName() + "\n" +
                    getString("label.doctor") + ": " + doctor.getFullName() + "\n" +
                    getString("label.service") + ": " + service.getServiceName() + "\n" +
                    getString("label.appointment_time") + ": " + startTime + " - " + endTime;

            if (reminderSent) {
                message += "\n\n✅ " + getString("info.reminder_will_be_sent") + "\n" +
                        "   📧 " + getString("label.email") + ": " + (patient.getEmail() != null ? patient.getEmail() : "не указан") + "\n" +
                        "   📞 " + getString("label.phone") + ": " + patient.getPhone();
            }

            showAlert(getString("status.success"), message);
            clearForm();
            busySlotsCache.clear();
            lastCheckedDate = null;
            lastCheckedDoctorId = null;
            updateTimeSlotsDisplay();
        } else {
            showAlert(getString("status.error"), getString("error.save_failed"));
        }
    }

    private void clearForm() {
        patientCombo.setValue(null);
        doctorCombo.setValue(null);
        serviceCombo.setValue(null);
        appointmentDatePicker.setValue(LocalDate.now());
        selectedTimeSlot = null;
        reminderSentCheck.setSelected(false);
        if (durationLabel != null) durationLabel.setText(getString("title.select_service_hint"));
        patientTable.getSelectionModel().clearSelection();
        doctorTable.getSelectionModel().clearSelection();
        clearSlotSelection();
        busySlotsCache.clear();
        lastCheckedDate = null;
        lastCheckedDoctorId = null;
        updateTimeSlotsDisplay();
    }
}