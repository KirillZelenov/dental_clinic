package ru.kafpin.dental_clinic.controller;

import javafx.scene.layout.GridPane;
import ru.kafpin.dental_clinic.dao.PaymentDAO;
import ru.kafpin.dental_clinic.dto.DebtDTO;
import ru.kafpin.dental_clinic.model.Payment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FinanceController extends BaseController implements Initializable {

    @FXML private TableView<Payment> paymentsTable;
    @FXML private TableView<DebtDTO> debtsTable;
    @FXML private Button addPaymentButton;
    @FXML private Button generateBillButton;
    @FXML private Button refreshFinanceButton;
    @FXML private Label debtsTitleLabel;
    @FXML private Label paymentsTitleLabel;

    private PaymentDAO paymentDAO;
    private ObservableList<Payment> paymentList = FXCollections.observableArrayList();
    private ObservableList<DebtDTO> debtList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        paymentDAO = new PaymentDAO();

        setupTableColumns();
        loadPayments();
        loadDebts();
        updateTexts();

        addPaymentButton.setOnAction(e -> addPayment());
        generateBillButton.setOnAction(e -> generateBill());
        refreshFinanceButton.setOnAction(e -> {
            loadPayments();
            loadDebts();
            showAlert(getString("status.success"), getString("info.refreshed"));
        });
    }

    @Override
    public void updateTexts() {
        updateButton(addPaymentButton, "button.add_payment");
        updateButton(generateBillButton, "button.generate_bill");
        updateButton(refreshFinanceButton, "button.refresh");

        if (debtsTitleLabel != null) debtsTitleLabel.setText(getString("title.debts_list"));
        if (paymentsTitleLabel != null) paymentsTitleLabel.setText(getString("title.payments_history"));

        updateTableColumnsText();
    }

    private void updateTableColumnsText() {
        if (debtsTable.getColumns().size() >= 4) {
            debtsTable.getColumns().get(0).setText(getString("column.appointment_id"));
            debtsTable.getColumns().get(1).setText(getString("column.patient"));
            debtsTable.getColumns().get(2).setText(getString("column.amount"));
            debtsTable.getColumns().get(3).setText(getString("column.payment_status"));
        }

        if (paymentsTable.getColumns().size() >= 5) {
            paymentsTable.getColumns().get(0).setText(getString("column.id"));
            paymentsTable.getColumns().get(1).setText(getString("column.appointment_id"));
            paymentsTable.getColumns().get(2).setText(getString("column.amount"));
            paymentsTable.getColumns().get(3).setText(getString("column.payment_date"));
            paymentsTable.getColumns().get(4).setText(getString("column.payment_status"));
        }
    }

    private void setupTableColumns() {
        debtsTable.getColumns().clear();

        TableColumn<DebtDTO, Long> debtIdCol = new TableColumn<>();
        TableColumn<DebtDTO, String> debtPatientCol = new TableColumn<>();
        TableColumn<DebtDTO, BigDecimal> debtAmountCol = new TableColumn<>();
        TableColumn<DebtDTO, String> debtStatusCol = new TableColumn<>();

        debtIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        debtPatientCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        debtAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        debtStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        debtIdCol.setPrefWidth(80);
        debtPatientCol.setPrefWidth(200);
        debtAmountCol.setPrefWidth(100);
        debtStatusCol.setPrefWidth(120);

        debtStatusCol.setCellFactory(column -> new TableCell<DebtDTO, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Оплачен".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("Частично оплачен".equals(item)) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        debtsTable.getColumns().addAll(debtIdCol, debtPatientCol, debtAmountCol, debtStatusCol);

        paymentsTable.getColumns().clear();

        TableColumn<Payment, Long> payIdCol = new TableColumn<>();
        TableColumn<Payment, Long> payAppointmentCol = new TableColumn<>();
        TableColumn<Payment, BigDecimal> payAmountCol = new TableColumn<>();
        TableColumn<Payment, LocalDate> payDateCol = new TableColumn<>();
        TableColumn<Payment, String> payStatusCol = new TableColumn<>();

        payIdCol.setCellValueFactory(new PropertyValueFactory<>("paymentId"));
        payAppointmentCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        payAmountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        payDateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        payStatusCol.setCellValueFactory(new PropertyValueFactory<>("paymentStatus"));

        payIdCol.setPrefWidth(60);
        payAppointmentCol.setPrefWidth(80);
        payAmountCol.setPrefWidth(100);
        payDateCol.setPrefWidth(100);
        payStatusCol.setPrefWidth(100);

        payStatusCol.setCellFactory(column -> new TableCell<Payment, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Оплачен".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if ("Частично оплачен".equals(item)) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });

        paymentsTable.getColumns().addAll(payIdCol, payAppointmentCol, payAmountCol, payDateCol, payStatusCol);
    }

    private void loadPayments() {
        try {
            List<Payment> payments = paymentDAO.getAll();
            paymentList.setAll(payments);
            paymentsTable.setItems(paymentList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(getString("status.error"), getString("error.load_payments") + ": " + e.getMessage());
        }
    }

    private void loadDebts() {
        try {
            List<DebtDTO> debts = paymentDAO.getUnpaidAppointments();
            debtList.setAll(debts);
            debtsTable.setItems(debtList);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(getString("status.error"), getString("error.load_debts") + ": " + e.getMessage());
        }
    }

    private void addPayment() {
        Dialog<Payment> dialog = new Dialog<>();
        dialog.setTitle(getString("title.add_payment"));

        ButtonType saveButtonType = new ButtonType(getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField appointmentIdField = new TextField();
        appointmentIdField.setPromptText(getString("prompt.appointment_id"));
        TextField amountField = new TextField();
        amountField.setPromptText(getString("prompt.amount"));
        DatePicker paymentDatePicker = new DatePicker(LocalDate.now());
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(
                "Не оплачен", "Частично оплачен", "Оплачен"
        ));
        statusCombo.setValue("Оплачен");

        grid.add(new Label(getString("label.appointment_id") + ":"), 0, 0);
        grid.add(appointmentIdField, 1, 0);
        grid.add(new Label(getString("label.amount") + ":"), 0, 1);
        grid.add(amountField, 1, 1);
        grid.add(new Label(getString("label.payment_date") + ":"), 0, 2);
        grid.add(paymentDatePicker, 1, 2);
        grid.add(new Label(getString("label.payment_status") + ":"), 0, 3);
        grid.add(statusCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Payment p = new Payment();
                    p.setAppointmentId(Long.parseLong(appointmentIdField.getText()));
                    p.setAmount(new BigDecimal(amountField.getText()));
                    p.setPaymentDate(paymentDatePicker.getValue());
                    p.setPaymentStatus(statusCombo.getValue());
                    return p;
                } catch (NumberFormatException e) {
                    showAlert(getString("status.error"), getString("error.invalid_number"));
                    return null;
                }
            }
            return null;
        });

        Optional<Payment> result = dialog.showAndWait();
        result.ifPresent(p -> {
            boolean success = paymentDAO.insert(p);
            if (success) {
                showAlert(getString("status.success"), getString("info.save_success"));
                loadPayments();
                loadDebts();
            } else {
                showAlert(getString("status.error"), getString("error.save_failed"));
            }
        });
    }

    private void generateBill() {
        DebtDTO selected = debtsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(getString("status.error"), getString("error.select_debt"));
            return;
        }

        String bill = "═══════════════════════════════════════════════════\n" +
                "                    " + getString("label.bill") + " №" + selected.getAppointmentId() + "\n" +
                "═══════════════════════════════════════════════════\n" +
                getString("label.patient") + ": " + selected.getPatientName() + "\n" +
                getString("label.amount") + ": " + selected.getAmount() + " " + getString("label.rub") + "\n" +
                getString("label.payment_status") + ": " + selected.getPaymentStatus() + "\n" +
                getString("label.date") + ": " + LocalDate.now() + "\n" +
                "═══════════════════════════════════════════════════\n" +
                getString("label.thanks") + "\n" +
                "═══════════════════════════════════════════════════";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getString("label.bill"));
        alert.setHeaderText(null);
        alert.setContentText(bill);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
}