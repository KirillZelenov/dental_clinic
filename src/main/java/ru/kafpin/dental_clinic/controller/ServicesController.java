package ru.kafpin.dental_clinic.controller;

import ru.kafpin.dental_clinic.dao.ServiceDAO;
import ru.kafpin.dental_clinic.model.Service;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ServicesController extends BaseController implements Initializable {

    @FXML private TableView<Service> servicesTable;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button addButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;
    @FXML private Button refreshButton;

    private ServiceDAO serviceDAO;
    private ObservableList<Service> serviceList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serviceDAO = new ServiceDAO();

        setupTableColumns();
        loadServices();
        updateTexts();

        searchButton.setOnAction(e -> searchServices());
        addButton.setOnAction(e -> showServiceDialog(null));
        editButton.setOnAction(e -> {
            Service selected = servicesTable.getSelectionModel().getSelectedItem();
            if (selected != null) showServiceDialog(selected);
            else showAlert(getString("status.error"), getString("error.select_service"));
        });
        deleteButton.setOnAction(e -> deleteService());
        refreshButton.setOnAction(e -> loadServices());

        searchField.textProperty().addListener((obs, old, val) -> searchServices());
    }

    private void setupTableColumns() {
        servicesTable.getColumns().clear();

        TableColumn<Service, Long> idColumn = new TableColumn<>();
        TableColumn<Service, String> nameColumn = new TableColumn<>();
        TableColumn<Service, BigDecimal> costColumn = new TableColumn<>();
        TableColumn<Service, Integer> durationColumn = new TableColumn<>();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("serviceId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("serviceName"));
        costColumn.setCellValueFactory(new PropertyValueFactory<>("cost"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("avgDurationMinutes"));

        idColumn.setPrefWidth(50);
        nameColumn.setPrefWidth(250);
        costColumn.setPrefWidth(100);
        durationColumn.setPrefWidth(100);

        servicesTable.getColumns().addAll(idColumn, nameColumn, costColumn, durationColumn);
    }

    @Override
    public void updateTexts() {
        updateButton(searchButton, "button.search");
        updateButton(addButton, "button.add");
        updateButton(editButton, "button.edit");
        updateButton(deleteButton, "button.delete");
        updateButton(refreshButton, "button.refresh");
        updateTextFieldPrompt(searchField, "label.search_service");

        // Обновляем заголовки столбцов
        updateTableColumnsText();
    }

    private void updateTableColumnsText() {
        if (servicesTable.getColumns().size() >= 4) {
            servicesTable.getColumns().get(0).setText(getString("column.id"));
            servicesTable.getColumns().get(1).setText(getString("column.service_name"));
            servicesTable.getColumns().get(2).setText(getString("column.cost"));
            servicesTable.getColumns().get(3).setText(getString("column.duration"));
        }
    }

    private void loadServices() {
        try {
            List<Service> services = serviceDAO.getAll();
            serviceList.setAll(services);
            servicesTable.setItems(serviceList);
        } catch (Exception e) {
            showAlert(getString("status.error"), getString("error.load_services") + ": " + e.getMessage());
        }
    }

    private void searchServices() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) loadServices();
        else {
            List<Service> filtered = serviceDAO.getAll().stream()
                    .filter(s -> s.getServiceName().toLowerCase().contains(keyword))
                    .toList();
            serviceList.setAll(filtered);
            servicesTable.setItems(serviceList);
        }
    }

    private void validateService(Service service) throws Exception {
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            throw new Exception(getString("error.empty_service_name"));
        }
        if (service.getCost() == null || service.getCost().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Exception(getString("error.invalid_cost"));
        }
        if (service.getAvgDurationMinutes() == null || service.getAvgDurationMinutes() <= 0) {
            throw new Exception(getString("error.invalid_duration"));
        }
    }

    private void showServiceDialog(Service service) {
        Dialog<Service> dialog = new Dialog<>();
        dialog.setTitle(service == null ? getString("title.add_service") : getString("title.edit_service"));

        ButtonType saveButtonType = new ButtonType(getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText(getString("prompt.service_name"));
        TextField costField = new TextField();
        costField.setPromptText(getString("prompt.cost"));
        TextField durationField = new TextField();
        durationField.setPromptText(getString("prompt.duration"));

        if (service != null) {
            nameField.setText(service.getServiceName());
            costField.setText(service.getCost().toString());
            durationField.setText(service.getAvgDurationMinutes().toString());
        }

        grid.add(new Label(getString("label.service_name") + ":*"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label(getString("label.cost") + " (" + getString("label.rub") + "):*"), 0, 1);
        grid.add(costField, 1, 1);
        grid.add(new Label(getString("label.duration") + " (" + getString("label.minutes") + "):*"), 0, 2);
        grid.add(durationField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Service s = service == null ? new Service() : service;
                    s.setServiceName(nameField.getText().trim());
                    s.setCost(new BigDecimal(costField.getText()));
                    s.setAvgDurationMinutes(Integer.parseInt(durationField.getText()));
                    return s;
                } catch (NumberFormatException e) {
                    showAlert(getString("status.error"), getString("error.invalid_number"));
                    return null;
                }
            }
            return null;
        });

        Optional<Service> result = dialog.showAndWait();
        result.ifPresent(s -> {
            try {
                validateService(s);
                boolean success = (service == null) ? serviceDAO.insert(s) : serviceDAO.update(s);
                if (success) {
                    showAlert(getString("status.success"), getString("info.save_success"));
                    loadServices();
                } else {
                    showAlert(getString("status.error"), getString("error.save_failed"));
                }
            } catch (Exception e) {
                showAlert(getString("status.error"), e.getMessage());
            }
        });
    }

    private void deleteService() {
        Service selected = servicesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(getString("status.error"), getString("error.select_service"));
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getString("title.confirm_delete"));
        alert.setHeaderText(null);
        alert.setContentText(getString("confirm.delete_service") + " " + selected.getServiceName() + "?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = serviceDAO.delete(selected.getServiceId());
            if (success) {
                showAlert(getString("status.success"), getString("info.delete_success"));
                loadServices();
            } else {
                showAlert(getString("status.error"), getString("error.delete_failed"));
            }
        }
    }
}