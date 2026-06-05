package ru.kafpin.dental_clinic.controller;

import ru.kafpin.dental_clinic.dao.PatientDAO;
import ru.kafpin.dental_clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class PatientsController extends BaseController implements Initializable {

    @FXML private TableView<Patient> patientsTable;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    private PatientDAO patientDAO;
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientDAO = new PatientDAO();

        setupTableColumns();
        loadPatients();
        updateTexts();

        searchButton.setOnAction(e -> searchPatients());
        addButton.setOnAction(e -> showPatientDialog(null));
        editButton.setOnAction(e -> {
            Patient selected = patientsTable.getSelectionModel().getSelectedItem();
            if (selected != null) showPatientDialog(selected);
            else showAlert(getString("status.error"), getString("error.select_patient"));
        });
        deleteButton.setOnAction(e -> deletePatient());
        refreshButton.setOnAction(e -> loadPatients());

        searchField.textProperty().addListener((obs, old, val) -> searchPatients());
    }

    @Override
    public void updateTexts() {
        updateButton(searchButton, "button.search");
        updateButton(addButton, "button.add");
        updateButton(editButton, "button.edit");
        updateButton(deleteButton, "button.delete");
        updateButton(refreshButton, "button.refresh");

        if (searchField != null) {
            searchField.setPromptText(getString("label.search_patient"));
        }

        updateTableColumnsText();
    }

    private void updateTableColumnsText() {
        if (patientsTable.getColumns().size() >= 8) {
            patientsTable.getColumns().get(0).setText(getString("column.id"));
            patientsTable.getColumns().get(1).setText(getString("column.full_name"));
            patientsTable.getColumns().get(2).setText(getString("column.birth_date"));
            patientsTable.getColumns().get(3).setText(getString("column.phone"));
            patientsTable.getColumns().get(4).setText("Email");
            patientsTable.getColumns().get(5).setText(getString("column.insurance_policy"));
            patientsTable.getColumns().get(6).setText(getString("column.allergies"));
            patientsTable.getColumns().get(7).setText(getString("column.contraindications"));
        }
    }

    private void setupTableColumns() {
        patientsTable.getColumns().clear();

        TableColumn<Patient, Long> idColumn = new TableColumn<>();
        TableColumn<Patient, String> nameColumn = new TableColumn<>();
        TableColumn<Patient, LocalDate> birthDateColumn = new TableColumn<>();
        TableColumn<Patient, String> phoneColumn = new TableColumn<>();
        TableColumn<Patient, String> emailColumn = new TableColumn<>("Email");
        TableColumn<Patient, String> policyColumn = new TableColumn<>();
        TableColumn<Patient, String> allergiesColumn = new TableColumn<>();
        TableColumn<Patient, String> contraindicationsColumn = new TableColumn<>();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        birthDateColumn.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        policyColumn.setCellValueFactory(new PropertyValueFactory<>("insurancePolicy"));
        allergiesColumn.setCellValueFactory(new PropertyValueFactory<>("allergies"));
        contraindicationsColumn.setCellValueFactory(new PropertyValueFactory<>("contraindications"));

        idColumn.setPrefWidth(50);
        nameColumn.setPrefWidth(200);
        birthDateColumn.setPrefWidth(100);
        phoneColumn.setPrefWidth(120);
        emailColumn.setPrefWidth(150);
        policyColumn.setPrefWidth(120);
        allergiesColumn.setPrefWidth(100);
        contraindicationsColumn.setPrefWidth(100);

        birthDateColumn.setCellFactory(column -> new TableCell<Patient, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            }
        });

        patientsTable.getColumns().addAll(idColumn, nameColumn, birthDateColumn, phoneColumn,
                emailColumn, policyColumn, allergiesColumn, contraindicationsColumn);

        updateTableColumnsText();
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientDAO.getAll();
            patientList.setAll(patients);
            patientsTable.setItems(patientList);
        } catch (Exception e) {
            showAlert(getString("status.error"), getString("error.load_patients") + ": " + e.getMessage());
        }
    }

    private void searchPatients() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPatients();
        } else {
            try {
                List<Patient> result = patientDAO.search(keyword);
                patientList.setAll(result);
                patientsTable.setItems(patientList);
                if (result.isEmpty()) {
                    showAlert(getString("status.info"), getString("info.no_records"));
                }
            } catch (Exception e) {
                showAlert(getString("status.error"), getString("error.search_failed") + ": " + e.getMessage());
            }
        }
    }

    private void showPatientDialog(Patient patient) {
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle(patient == null ? getString("title.add_patient") : getString("title.edit_patient"));

        ButtonType saveButtonType = new ButtonType(getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField fullNameField = new TextField();
        fullNameField.setId("fullNameField");
        fullNameField.setPromptText(getString("prompt.full_name"));

        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setId("birthDatePicker");
        birthDatePicker.setPromptText(getString("label.birth_date"));

        TextField phoneField = new TextField();
        phoneField.setId("phoneField");
        phoneField.setPromptText(getString("prompt.phone"));

        TextField emailField = new TextField();
        emailField.setId("emailField");
        emailField.setPromptText(getString("prompt.email"));

        TextField policyField = new TextField();
        policyField.setId("policyField");
        policyField.setPromptText(getString("prompt.policy"));

        TextArea allergiesArea = new TextArea();
        allergiesArea.setId("allergiesArea");
        allergiesArea.setPromptText(getString("prompt.allergies"));

        TextArea contraindicationsArea = new TextArea();
        contraindicationsArea.setId("contraindicationsArea");
        contraindicationsArea.setPromptText(getString("prompt.contraindications"));

        if (patient != null) {
            fullNameField.setText(patient.getFullName());
            birthDatePicker.setValue(patient.getBirthDate());
            phoneField.setText(patient.getPhone());
            emailField.setText(patient.getEmail());
            policyField.setText(patient.getInsurancePolicy());
            allergiesArea.setText(patient.getAllergies());
            contraindicationsArea.setText(patient.getContraindications());
        } else {
            birthDatePicker.setValue(LocalDate.of(2000, 1, 1));
        }

        grid.add(new Label(getString("label.full_name") + ":*"), 0, 0);
        grid.add(fullNameField, 1, 0);
        grid.add(new Label(getString("label.birth_date") + ":*"), 0, 1);
        grid.add(birthDatePicker, 1, 1);
        grid.add(new Label(getString("label.phone") + ":*"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("Email:*"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label(getString("label.insurance_policy") + ":*"), 0, 4);
        grid.add(policyField, 1, 4);
        grid.add(new Label(getString("label.allergies") + ":"), 0, 5);
        grid.add(allergiesArea, 1, 5);
        grid.add(new Label(getString("label.contraindications") + ":"), 0, 6);
        grid.add(contraindicationsArea, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String fullName = fullNameField.getText().trim();

                if (fullName.isEmpty()) {
                    showAlert(getString("status.error"), getString("error.empty_fullname"));
                    return null;
                }

                Patient p = patient == null ? new Patient() : patient;
                p.setFullName(fullName);
                p.setBirthDate(birthDatePicker.getValue());
                p.setPhone(phoneField.getText().trim());
                p.setEmail(emailField.getText().trim());
                p.setInsurancePolicy(policyField.getText().trim());
                p.setAllergies(allergiesArea.getText().trim().isEmpty() ? getString("label.none") : allergiesArea.getText().trim());
                p.setContraindications(contraindicationsArea.getText().trim().isEmpty() ? getString("label.none") : contraindicationsArea.getText().trim());
                return p;
            }
            return null;
        });

        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(p -> {
            try {
                boolean success = (patient == null) ? patientDAO.insert(p) : patientDAO.update(p);
                if (success) {
                    showAlert(getString("status.success"), getString("info.save_success"));
                    loadPatients();
                } else {
                    showAlert(getString("status.error"), getString("error.save_failed"));
                }
            } catch (Exception e) {
                showAlert(getString("status.error"), getString("error.save_failed") + ": " + e.getMessage());
            }
        });
    }

    private void deletePatient() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(getString("status.error"), getString("error.select_patient"));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getString("title.confirm_delete"));
        alert.setHeaderText(null);
        alert.setContentText(getString("confirm.delete_patient") + " \"" + selected.getFullName() + "\"?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                boolean success = patientDAO.delete(selected.getPatientId());
                if (success) {
                    showAlert(getString("status.success"), getString("info.delete_success"));
                    loadPatients();
                } else {
                    showAlert(getString("status.error"), getString("error.delete_failed"));
                }
            } catch (Exception e) {
                showAlert(getString("status.error"), getString("error.delete_failed") + ": " + e.getMessage());
            }
        }
    }

    protected String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
}