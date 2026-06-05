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

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginDialog {
    private static final Logger logger = LoggerFactory.getLogger(LoginDialog.class);
    private ResourceBundle bundle;
    private String currentLanguage;

    public LoginDialog() {
        currentLanguage = "Русский";
        bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("ru", "RU"));
    }

    public Optional<LoginResult> showAndWait() {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle("Авторизация");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        Label titleLabel = new Label("Вход в систему");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label usernameLabel = new Label("Логин:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Введите логин");

        Label passwordLabel = new Label("Пароль:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");

        Label languageLabel = new Label("Язык:");
        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Русский", "English", "Deutsch");
        languageCombo.setValue("Русский");

        Button loginButton = new Button("Войти");
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        Button registerButton = new Button("Регистрация");
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        Button cancelButton = new Button("Отмена");

        languageCombo.setOnAction(e -> {
            String selected = languageCombo.getValue();
            currentLanguage = selected;
            if ("Русский".equals(selected)) {
                bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("ru", "RU"));
                updateRussianTexts(dialogStage, titleLabel, usernameLabel, usernameField,
                        passwordLabel, passwordField, languageLabel, loginButton,
                        registerButton, cancelButton);
            } else if ("Deutsch".equals(selected)) {
                bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("de", "DE"));
                updateGermanTexts(dialogStage, titleLabel, usernameLabel, usernameField,
                        passwordLabel, passwordField, languageLabel, loginButton,
                        registerButton, cancelButton);
            } else {
                bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("en", "US"));
                updateEnglishTexts(dialogStage, titleLabel, usernameLabel, usernameField,
                        passwordLabel, passwordField, languageLabel, loginButton,
                        registerButton, cancelButton);
            }
        });

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(languageLabel, 0, 3);
        grid.add(languageCombo, 1, 3);

        HBox buttonBar = new HBox(10, loginButton, registerButton, cancelButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(15, grid, buttonBar);
        root.setPadding(new Insets(20));

        final String[] username = {null};
        final String[] password = {null};
        final boolean[] okClicked = {false};

        loginButton.setOnAction(e -> {
            String u = usernameField.getText().trim();
            String p = passwordField.getText();

            if (u.isEmpty()) {
                showAlert(getString("error.title"), getString("error.empty_username"));
                return;
            }
            if (p.isEmpty()) {
                showAlert(getString("error.title"), getString("error.empty_password"));
                return;
            }

            username[0] = u;
            password[0] = p;
            okClicked[0] = true;
            dialogStage.close();
        });

        registerButton.setOnAction(e -> {
            RegistrationDialog regDialog = new RegistrationDialog();
            Optional<RegistrationDialog.RegistrationResult> regResult = regDialog.showAndWait();

            if (regResult.isPresent()) {
                usernameField.setText(regResult.get().getUsername());
                passwordField.setText(regResult.get().getPassword());
                showAlert(getString("status.success"), getString("success.registered"));
            }
        });

        cancelButton.setOnAction(e -> {
            okClicked[0] = false;
            dialogStage.close();
        });

        dialogStage.setScene(new Scene(root, 450, 350));
        dialogStage.showAndWait();

        if (okClicked[0]) {
            logger.info("Login attempt for user: {}", username[0]);
            return Optional.of(new LoginResult(username[0], password[0]));
        }
        logger.info("Login cancelled by user");
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
                                    Label passwordLabel, PasswordField passwordField, Label langLabel,
                                    Button loginBtn, Button registerBtn, Button cancelBtn) {
        stage.setTitle("Авторизация");
        title.setText("Вход в систему");
        usernameLabel.setText("Логин:");
        usernameField.setPromptText("Введите логин");
        passwordLabel.setText("Пароль:");
        passwordField.setPromptText("Введите пароль");
        langLabel.setText("Язык:");
        loginBtn.setText("Войти");
        registerBtn.setText("Регистрация");
        cancelBtn.setText("Отмена");
    }

    private void updateEnglishTexts(Stage stage, Label title, Label usernameLabel, TextField usernameField,
                                    Label passwordLabel, PasswordField passwordField, Label langLabel,
                                    Button loginBtn, Button registerBtn, Button cancelBtn) {
        stage.setTitle("Authorization");
        title.setText("Login");
        usernameLabel.setText("Username:");
        usernameField.setPromptText("Enter username");
        passwordLabel.setText("Password:");
        passwordField.setPromptText("Enter password");
        langLabel.setText("Language:");
        loginBtn.setText("Login");
        registerBtn.setText("Register");
        cancelBtn.setText("Cancel");
    }

    private void updateGermanTexts(Stage stage, Label title, Label usernameLabel, TextField usernameField,
                                   Label passwordLabel, PasswordField passwordField, Label langLabel,
                                   Button loginBtn, Button registerBtn, Button cancelBtn) {
        stage.setTitle("Autorisierung");
        title.setText("Anmeldung");
        usernameLabel.setText("Benutzername:");
        usernameField.setPromptText("Benutzername eingeben");
        passwordLabel.setText("Passwort:");
        passwordField.setPromptText("Passwort eingeben");
        langLabel.setText("Sprache:");
        loginBtn.setText("Anmelden");
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

    public static class LoginResult {
        private final String username;
        private final String password;

        public LoginResult(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
    }
}