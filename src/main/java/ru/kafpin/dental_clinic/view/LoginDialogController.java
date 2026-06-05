package ru.kafpin.dental_clinic.view;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginDialogController {
    private static final Logger logger = LoggerFactory.getLogger(LoginDialogController.class);

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button okButton;
    @FXML private Button cancelButton;
    @FXML private Label titleLabel;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label languageLabel;

    private Stage dialogStage;
    private boolean okClicked = false;
    private String username;
    private String password;

    @FXML
    public void initialize() {

        languageCombo.getItems().addAll("Русский", "English", "Deutsch");
        languageCombo.setValue("Русский");


        updateRussianTexts();

        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            if ("English".equals(selected)) {
                updateEnglishTexts();
            } else if ("Deutsch".equals(selected)) {
                updateGermanTexts();
            } else {
                updateRussianTexts();
            }
        });
    }

    private void updateRussianTexts() {
        titleLabel.setText("Вход в систему");
        usernameLabel.setText("Логин:");
        passwordLabel.setText("Пароль:");
        languageLabel.setText("Язык:");
        okButton.setText("Войти");
        cancelButton.setText("Отмена");
        if (dialogStage != null) {
            dialogStage.setTitle("Авторизация");
        }
    }

    private void updateEnglishTexts() {
        titleLabel.setText("Login");
        usernameLabel.setText("Username:");
        passwordLabel.setText("Password:");
        languageLabel.setText("Language:");
        okButton.setText("Login");
        cancelButton.setText("Cancel");
        if (dialogStage != null) {
            dialogStage.setTitle("Authorization");
        }
    }

    private void updateGermanTexts() {
        titleLabel.setText("Anmeldung");
        usernameLabel.setText("Benutzername:");
        passwordLabel.setText("Passwort:");
        languageLabel.setText("Sprache:");
        okButton.setText("Anmelden");
        cancelButton.setText("Abbrechen");
        if (dialogStage != null) {
            dialogStage.setTitle("Autorisierung");
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
        updateRussianTexts();
    }

    @FXML
    private void handleOk() {
        username = usernameField.getText().trim();
        password = passwordField.getText();

        if (username.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Логин не может быть пустым!");
            alert.showAndWait();
            return;
        }

        if (password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Пароль не может быть пустым!");
            alert.showAndWait();
            return;
        }

        okClicked = true;
        dialogStage.close();
    }

    @FXML
    private void handleCancel() {
        okClicked = false;
        dialogStage.close();
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}