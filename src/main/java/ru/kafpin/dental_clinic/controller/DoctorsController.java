package ru.kafpin.dental_clinic.controller;

import ru.kafpin.dental_clinic.dao.DoctorDAO;
import ru.kafpin.dental_clinic.model.Doctor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class DoctorsController extends BaseController implements Initializable {

    @FXML private TableView<Doctor> doctorsTable;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    private DoctorDAO doctorDAO;
    private ObservableList<Doctor> doctorList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        doctorDAO = new DoctorDAO();

        setupTableColumns();
        loadDoctors();
        updateTexts();

        searchButton.setOnAction(e -> searchDoctors());
        addButton.setOnAction(e -> showDoctorDialog(null));
        editButton.setOnAction(e -> {
            Doctor selected = doctorsTable.getSelectionModel().getSelectedItem();
            if (selected != null) showDoctorDialog(selected);
            else showAlert(getString("status.error"), getString("error.select_doctor"));
        });
        deleteButton.setOnAction(e -> deleteDoctor());
        refreshButton.setOnAction(e -> loadDoctors());

        searchField.textProperty().addListener((obs, old, val) -> searchDoctors());
    }

    private void setupTableColumns() {
        doctorsTable.getColumns().clear();

        TableColumn<Doctor, Long> idColumn = new TableColumn<>();
        TableColumn<Doctor, String> nameColumn = new TableColumn<>();
        TableColumn<Doctor, String> specializationColumn = new TableColumn<>();
        TableColumn<Doctor, String> scheduleColumn = new TableColumn<>();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("doctorId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        specializationColumn.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        scheduleColumn.setCellValueFactory(new PropertyValueFactory<>("workSchedule"));

        idColumn.setPrefWidth(50);
        nameColumn.setPrefWidth(200);
        specializationColumn.setPrefWidth(150);
        scheduleColumn.setPrefWidth(150);

        doctorsTable.getColumns().addAll(idColumn, nameColumn, specializationColumn, scheduleColumn);

    }

    @Override
    public void updateTexts() {
        updateButton(searchButton, "button.search");
        updateButton(addButton, "button.add");
        updateButton(editButton, "button.edit");
        updateButton(deleteButton, "button.delete");
        updateButton(refreshButton, "button.refresh");
        updateTextFieldPrompt(searchField, "label.search_doctor");

        updateTableColumnsText();
    }

    private void updateTableColumnsText() {
        if (doctorsTable.getColumns().size() >= 4) {
            doctorsTable.getColumns().get(0).setText(getString("column.id"));
            doctorsTable.getColumns().get(1).setText(getString("column.full_name"));
            doctorsTable.getColumns().get(2).setText(getString("column.specialization"));
            doctorsTable.getColumns().get(3).setText(getString("column.work_schedule"));
        }
    }

    private void loadDoctors() {
        try {
            List<Doctor> doctors = doctorDAO.getAll();
            doctorList.setAll(doctors);
            doctorsTable.setItems(doctorList);
        } catch (Exception e) {
            showAlert(getString("status.error"), getString("error.load_doctors") + ": " + e.getMessage());
        }
    }

    private void searchDoctors() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) loadDoctors();
        else {
            List<Doctor> filtered = doctorDAO.getAll().stream()
                    .filter(d -> d.getFullName().toLowerCase().contains(keyword) ||
                            d.getSpecialization().toLowerCase().contains(keyword))
                    .toList();
            doctorList.setAll(filtered);
            doctorsTable.setItems(doctorList);
        }
    }

    private void validateDoctor(Doctor doctor) throws Exception {
        if (doctor.getFullName() == null || doctor.getFullName().trim().isEmpty()) {
            throw new Exception(getString("error.empty_doctor_name"));
        }
        if (doctor.getSpecialization() == null || doctor.getSpecialization().trim().isEmpty()) {
            throw new Exception(getString("error.empty_specialization"));
        }
    }

    private void showDoctorDialog(Doctor doctor) {
        Dialog<Doctor> dialog = new Dialog<>();
        dialog.setTitle(doctor == null ? getString("title.add_doctor") : getString("title.edit_doctor"));

        ButtonType saveButtonType = new ButtonType(getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText(getString("prompt.doctor_name"));

        ComboBox<String> specCombo = new ComboBox<>();
        specCombo.setItems(FXCollections.observableArrayList(
                "Терапевт", "Хирург", "Ортодонт", "Ортопед", "Детский", "Пародонтолог", "Имплантолог"
        ));
        specCombo.setPromptText(getString("label.specialization"));

        TextField scheduleField = new TextField();
        scheduleField.setPromptText(getString("prompt.work_schedule"));

        if (doctor != null) {
            nameField.setText(doctor.getFullName());
            specCombo.setValue(doctor.getSpecialization());
            scheduleField.setText(doctor.getWorkSchedule());
        }

        grid.add(new Label(getString("label.full_name") + ":*"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label(getString("label.specialization") + ":*"), 0, 1);
        grid.add(specCombo, 1, 1);
        grid.add(new Label(getString("label.work_schedule") + ":"), 0, 2);
        grid.add(scheduleField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Doctor d = doctor == null ? new Doctor() : doctor;
                d.setFullName(nameField.getText().trim());
                d.setSpecialization(specCombo.getValue());
                d.setWorkSchedule(scheduleField.getText().trim());
                return d;
            }
            return null;
        });

        Optional<Doctor> result = dialog.showAndWait();
        result.ifPresent(d -> {
            try {
                validateDoctor(d);
                boolean success = (doctor == null) ? doctorDAO.insert(d) : doctorDAO.update(d);
                if (success) {
                    showAlert(getString("status.success"), getString("info.save_success"));
                    loadDoctors();
                } else {
                    showAlert(getString("status.error"), getString("error.save_failed"));
                }
            } catch (Exception e) {
                showAlert(getString("status.error"), e.getMessage());
            }
        });
    }

    private void deleteDoctor() {
        Doctor selected = doctorsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(getString("status.error"), getString("error.select_doctor"));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getString("title.confirm_delete"));
        alert.setHeaderText(null);
        alert.setContentText(getString("confirm.delete_doctor") + " " + selected.getFullName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = doctorDAO.delete(selected.getDoctorId());
            if (success) {
                showAlert(getString("status.success"), getString("info.delete_success"));
                loadDoctors();
            } else {
                showAlert(getString("status.error"), getString("error.delete_failed"));
            }
        }
    }
}