package ru.kafpin.dental_clinic.controller;

import javafx.scene.layout.GridPane;
import ru.kafpin.dental_clinic.dao.PatientDAO;
import ru.kafpin.dental_clinic.dao.TreatmentRecordDAO;
import ru.kafpin.dental_clinic.model.Patient;
import ru.kafpin.dental_clinic.model.TreatmentRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MedicalHistoryController extends BaseController implements Initializable {

    @FXML private ComboBox<Patient> patientHistoryCombo;
    @FXML private TableView<TreatmentRecord> historyTable;
    @FXML private Button addTreatmentButton;
    @FXML private Button editTreatmentButton;
    @FXML private Button loadHistoryButton;
    @FXML private Label selectPatientLabel;

    private PatientDAO patientDAO;
    private TreatmentRecordDAO treatmentRecordDAO;
    private ObservableList<TreatmentRecord> treatmentList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        patientDAO = new PatientDAO();
        treatmentRecordDAO = new TreatmentRecordDAO();

        setupTableColumns();
        loadPatients();
        updateTexts();

        patientHistoryCombo.setOnAction(e -> loadHistory());
        loadHistoryButton.setOnAction(e -> loadHistory());
        addTreatmentButton.setOnAction(e -> showTreatmentDialog(null));
        editTreatmentButton.setOnAction(e -> {
            TreatmentRecord selected = historyTable.getSelectionModel().getSelectedItem();
            if (selected != null) showTreatmentDialog(selected);
            else showAlert(getString("status.error"), getString("error.select_treatment"));
        });
    }

    @Override
    public void updateTexts() {
        updateButton(addTreatmentButton, "button.add");
        updateButton(editTreatmentButton, "button.edit");
        updateButton(loadHistoryButton, "button.show_history");

        if (selectPatientLabel != null) selectPatientLabel.setText(getString("label.select_patient") + ":");
        if (patientHistoryCombo != null) {
            patientHistoryCombo.setPromptText(getString("label.select_patient"));
        }

        updateTableColumnsText();
        historyTable.setPlaceholder(new Label(getString("title.no_history")));
    }

    private void updateTableColumnsText() {
        if (historyTable.getColumns().size() >= 6) {
            historyTable.getColumns().get(0).setText(getString("column.id"));
            historyTable.getColumns().get(1).setText(getString("column.appointment_id"));
            historyTable.getColumns().get(2).setText(getString("column.tooth_status"));
            historyTable.getColumns().get(3).setText(getString("column.performed_work"));
            historyTable.getColumns().get(4).setText(getString("column.prescriptions"));
            historyTable.getColumns().get(5).setText(getString("column.created_date"));
        }
    }

    private void setupTableColumns() {
        historyTable.getColumns().clear();

        TableColumn<TreatmentRecord, Long> idCol = new TableColumn<>();
        TableColumn<TreatmentRecord, Long> appointmentCol = new TableColumn<>();
        TableColumn<TreatmentRecord, String> toothCol = new TableColumn<>();
        TableColumn<TreatmentRecord, String> workCol = new TableColumn<>();
        TableColumn<TreatmentRecord, String> prescriptionsCol = new TableColumn<>();
        TableColumn<TreatmentRecord, String> createdCol = new TableColumn<>();

        idCol.setCellValueFactory(new PropertyValueFactory<>("treatmentId"));
        appointmentCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        toothCol.setCellValueFactory(new PropertyValueFactory<>("toothStatus"));
        workCol.setCellValueFactory(new PropertyValueFactory<>("performedWork"));
        prescriptionsCol.setCellValueFactory(new PropertyValueFactory<>("prescriptions"));
        createdCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        idCol.setPrefWidth(50);
        appointmentCol.setPrefWidth(80);
        toothCol.setPrefWidth(120);
        workCol.setPrefWidth(200);
        prescriptionsCol.setPrefWidth(200);
        createdCol.setPrefWidth(150);

        historyTable.getColumns().addAll(idCol, appointmentCol, toothCol, workCol, prescriptionsCol, createdCol);

        updateTableColumnsText();

        historyTable.setPlaceholder(new Label(getString("title.no_history")));
    }

    private void loadPatients() {
        List<Patient> patients = patientDAO.getAll();
        patientHistoryCombo.setItems(FXCollections.observableArrayList(patients));
    }

    private void loadHistory() {
        Patient selected = patientHistoryCombo.getValue();
        if (selected != null) {
            List<TreatmentRecord> records = treatmentRecordDAO.getByPatientId(selected.getPatientId());
            treatmentList.setAll(records);
            historyTable.setItems(treatmentList);
            if (records.isEmpty()) {
                showAlert(getString("status.info"), getString("info.no_history"));
            }
        } else {
            showAlert(getString("status.warning"), getString("error.select_patient"));
        }
    }

    private void showTreatmentDialog(TreatmentRecord record) {
        Dialog<TreatmentRecord> dialog = new Dialog<>();
        dialog.setTitle(record == null ? getString("title.add_treatment") : getString("title.edit_treatment"));

        ButtonType saveButtonType = new ButtonType(getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField toothStatusField = new TextField();
        toothStatusField.setPromptText(getString("prompt.tooth_status"));
        TextArea performedWorkArea = new TextArea();
        performedWorkArea.setPromptText(getString("prompt.performed_work"));
        TextArea prescriptionsArea = new TextArea();
        prescriptionsArea.setPromptText(getString("prompt.prescriptions"));

        if (record != null) {
            toothStatusField.setText(record.getToothStatus());
            performedWorkArea.setText(record.getPerformedWork());
            prescriptionsArea.setText(record.getPrescriptions());
        }

        grid.add(new Label(getString("label.tooth_status") + ":"), 0, 0);
        grid.add(toothStatusField, 1, 0);
        grid.add(new Label(getString("label.performed_work") + ":"), 0, 1);
        grid.add(performedWorkArea, 1, 1);
        grid.add(new Label(getString("label.prescriptions") + ":"), 0, 2);
        grid.add(prescriptionsArea, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                TreatmentRecord tr = record == null ? new TreatmentRecord() : record;
                tr.setToothStatus(toothStatusField.getText());
                tr.setPerformedWork(performedWorkArea.getText());
                tr.setPrescriptions(prescriptionsArea.getText());
                return tr;
            }
            return null;
        });

        Optional<TreatmentRecord> result = dialog.showAndWait();
        result.ifPresent(tr -> {
            if (record == null) {
                Long appointmentId = selectAppointment();
                if (appointmentId != null) {
                    tr.setAppointmentId(appointmentId);
                    treatmentRecordDAO.insert(tr);
                    showAlert(getString("status.success"), getString("info.save_success"));
                }
            } else {
                treatmentRecordDAO.update(tr);
                showAlert(getString("status.success"), getString("info.save_success"));
            }
            loadHistory();
        });
    }

    private Long selectAppointment() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(getString("title.select_appointment"));
        dialog.setHeaderText(getString("label.enter_appointment_id"));
        dialog.setContentText(getString("label.appointment_id") + ":");
        Optional<String> result = dialog.showAndWait();
        try {
            return result.map(Long::parseLong).orElse(null);
        } catch (NumberFormatException e) {
            showAlert(getString("status.error"), getString("error.invalid_number"));
            return null;
        }
    }
}