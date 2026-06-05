package ru.kafpin.dental_clinic.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kafpin.dental_clinic.config.DatabaseInitializer;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class RegistrationDialog {
    private static final Logger logger = LoggerFactory.getLogger(RegistrationDialog.class);
    private ResourceBundle bundle;
    private String currentLanguage;

    public RegistrationDialog() {
        currentLanguage = "Русский";
        bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("ru", "RU"));
    }

    public Optional<RegistrationResult> showAndWait() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Регистрация нового пользователя");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        Label titleLabel = new Label("Регистрация нового пользователя");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label usernameLabel = new Label("Логин:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Введите логин (минимум 3 символа)");

        Label passwordLabel = new Label("Пароль:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль (минимум 4 символа)");

        Label confirmPasswordLabel = new Label("Подтверждение пароля:");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Повторите пароль");

        Label languageLabel = new Label("Язык:");
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Русский", "English", "Deutsch");
        languageCombo.setValue("Русский");

        Button registerButton = new Button("Зарегистрироваться");
        registerButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        Button cancelButton = new Button("Отмена");

        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            currentLanguage = selected;
            if ("Русский".equals(selected)) {
                bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("ru", "RU"));
                updateRussianTexts(dialogStage, titleLabel, usernameLabel, usernameField,
                        passwordLabel, passwordField, confirmPasswordLabel, confirmPasswordField,
                        languageLabel, registerButton, cancelButton);
            } else if ("Deutsch".equals(selected)) {
                bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("de", "DE"));
                updateGermanTexts(dialogStage, titleLabel, usernameLabel, usernameField,
                        passwordLabel, passwordField, confirmPasswordLabel, confirmPasswordField,
                        languageLabel, registerButton, cancelButton);
            } else {
                bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("en", "US"));
                updateEnglishTexts(dialogStage, titleLabel, usernameLabel, usernameField,
                        passwordLabel, passwordField, confirmPasswordLabel, confirmPasswordField,
                        languageLabel, registerButton, cancelButton);
            }
        });

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(confirmPasswordLabel, 0, 3);
        grid.add(confirmPasswordField, 1, 3);
        grid.add(languageLabel, 0, 4);
        grid.add(languageCombo, 1, 4);

        HBox buttonBar = new HBox(10, registerButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, grid, buttonBar);
        root.setPadding(new Insets(20));

        final boolean[] registered = {false};
        final String[] finalUsername = {null};
        final String[] finalPassword = {null};

        registerButton.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();
            String cp = confirmPasswordField.getText();

            if (u.isEmpty()) {
                showAlert(getString("error.title"), getString("error.empty_username"));
                return;
            }

            if (u.length() < 3) {
                showAlert(getString("error.title"), getString("error.short_username"));
                return;
            }

            if (p.isEmpty()) {
                showAlert(getString("error.title"), getString("error.empty_password"));
                return;
            }

            if (p.length() < 4) {
                showAlert(getString("error.title"), getString("error.short_password"));
                return;
            }

            if (!p.equals(cp)) {
                showAlert(getString("error.title"), getString("error.password_mismatch"));
                return;
            }

            if (!DatabaseInitializer.checkDatabaseExists()) {
                showAlert(getString("error.title"), getString("error.database"));
                return;
            }

            boolean created = DatabaseInitializer.createUserIfNotExists(u, p);
            if (created) {
                registered[0] = true;
                finalUsername[0] = u;
                finalPassword[0] = p;
                showAlert(getString("status.success"), getString("success.registered"));
                dialogStage.close();
            } else {
                showAlert(getString("error.title"), getString("error.user_exists"));
            }
        });

        cancelButton.setOnAction(e -> {
            registered[0] = false;
            dialogStage.close();
        });

        dialogStage.setScene(new Scene(root, 550, 420));
        dialogStage.showAndWait();

        if (registered[0]) {
            logger.info("New user registered: {}", finalUsername[0]);
            return Optional.of(new RegistrationResult(finalUsername[0], finalPassword[0]));
        }
        return Optional.empty();
    }

    private String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    private void updateRussianTexts(Stage stage, Label title, Label usernameLabel, TextField usernameField,
                                    Label passwordLabel, PasswordField passwordField, Label confirmLabel,
                                    PasswordField confirmField, Label langLabel, Button registerBtn, Button cancelBtn) {
        stage.setTitle("Регистрация нового пользователя");
        title.setText("Регистрация нового пользователя");
        usernameLabel.setText("Логин:");
        usernameField.setPromptText("Введите логин (минимум 3 символа)");
        passwordLabel.setText("Пароль:");
        passwordField.setPromptText("Введите пароль (минимум 4 символа)");
        confirmLabel.setText("Подтверждение пароля:");
        confirmField.setPromptText("Повторите пароль");
        langLabel.setText("Язык:");
        registerBtn.setText("Зарегистрироваться");
        cancelBtn.setText("Отмена");
    }

    private void updateEnglishTexts(Stage stage, Label title, Label usernameLabel, TextField usernameField,
                                    Label passwordLabel, PasswordField passwordField, Label confirmLabel,
                                    PasswordField confirmField, Label langLabel, Button registerBtn, Button cancelBtn) {
        stage.setTitle("Register New User");
        title.setText("Register New User");
        usernameLabel.setText("Username:");
        usernameField.setPromptText("Enter username (min 3 characters)");
        passwordLabel.setText("Password:");
        passwordField.setPromptText("Enter password (min 4 characters)");
        confirmLabel.setText("Confirm Password:");
        confirmField.setPromptText("Repeat password");
        langLabel.setText("Language:");
        registerBtn.setText("Register");
        cancelBtn.setText("Cancel");
    }

    private void updateGermanTexts(Stage stage, Label title, Label usernameLabel, TextField usernameField,
                                   Label passwordLabel, PasswordField passwordField, Label confirmLabel,
                                   PasswordField confirmField, Label langLabel, Button registerBtn, Button cancelBtn) {
        stage.setTitle("Benutzer registrieren");
        title.setText("Benutzer registrieren");
        usernameLabel.setText("Benutzername:");
        usernameField.setPromptText("Benutzername eingeben (min. 3 Zeichen)");
        passwordLabel.setText("Passwort:");
        passwordField.setPromptText("Passwort eingeben (min. 4 Zeichen)");
        confirmLabel.setText("Passwort bestätigen:");
        confirmField.setPromptText("Passwort wiederholen");
        langLabel.setText("Sprache:");
        registerBtn.setText("Registrieren");
        cancelBtn.setText("Abbrechen");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class RegistrationResult {
        private final String username;
        private final String password;

        public RegistrationResult(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}