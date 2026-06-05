package ru.kafpin.dental_clinic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.view.LoginDialog;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class Main extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Stage primaryStage;
    private ResourceBundle bundle;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        Locale.setDefault(new Locale("ru", "RU"));

        try {
            bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", Locale.getDefault());
        } catch (Exception e) {
            logger.error("Could not load resource bundle", e);
        }

        logger.info("Application started with Russian language");

        LoginDialog loginDialog = new LoginDialog();
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            Optional<LoginDialog.LoginResult> result = loginDialog.showAndWait();
            if (result.isEmpty()) {
                logger.info("User cancelled login, exiting application");
                Platform.exit();
                return;
            }

            String username = result.get().getUsername();
            String password = result.get().getPassword();

            try {
                DatabaseConfig.initConnection(username, password);
                logger.info("Successful connection for user: {}", username);
                break;
            } catch (SQLException ex) {
                attempts++;
                String errorMsg = ex.getMessage();
                logger.error("Connection error for user {} (attempt {}/{}): {}",
                        username, attempts, MAX_ATTEMPTS, errorMsg);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Не удалось подключиться к базе данных");

                String errorMessage = getErrorMessage(errorMsg, username, attempts, MAX_ATTEMPTS);
                alert.setContentText(errorMessage);
                alert.showAndWait();
            }
        }

        if (attempts >= MAX_ATTEMPTS) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Превышено максимальное количество попыток входа.\nПриложение будет закрыто.");
            alert.showAndWait();
            Platform.exit();
            return;
        }

        try {
            String fxmlPath = "/ru/kafpin/dental_clinic/view/main-app-view.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (bundle != null) {
                loader.setResources(bundle);
            }
            Parent root = loader.load();

            Scene scene = new Scene(root, 1200, 700);

            String cssPath = "/ru/kafpin/dental_clinic/css/style.css";
            if (getClass().getResource(cssPath) != null) {
                scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
            }

            primaryStage.setTitle("АРМ стоматологической клиники");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(e -> {
                DatabaseConfig.closeConnection();
                logger.info("Application closed");
            });
            primaryStage.show();
            logger.info("Main window displayed");

        } catch (Exception e) {
            logger.error("Error loading main window", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Ошибка загрузки главного окна:\n" + e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    private String getErrorMessage(String errorMsg, String username, int attempts, int maxAttempts) {
        String lowerMsg = errorMsg.toLowerCase();

        if (lowerMsg.contains("password") && lowerMsg.contains("authentication")) {
            return "Неверный пароль!\n\nПопытка " + attempts + " из " + maxAttempts;
        }

        if ((lowerMsg.contains("does not exist") || lowerMsg.contains("role")) && lowerMsg.contains("user")) {
            return "Пользователь \"" + username + "\" не существует!\n\n" +
                    "1. Проверьте правильность ввода логина\n" +
                    "2. Или нажмите кнопку 'Регистрация' для создания нового пользователя\n\n" +
                    "Попытка " + attempts + " из " + maxAttempts;
        }

        if (lowerMsg.contains("connection refused") || lowerMsg.contains("timeout")) {
            return "Не удалось подключиться к серверу PostgreSQL!\n\n" +
                    "Проверьте:\n" +
                    "1. Запущен ли сервер PostgreSQL\n" +
                    "2. Правильно ли указан порт (по умолчанию 5432)\n" +
                    "3. Не блокирует ли подключение брандмауэр\n\n" +
                    "Попытка " + attempts + " из " + maxAttempts;
        }

        if (lowerMsg.contains("database") && lowerMsg.contains("does not exist")) {
            return "База данных \"dental_clinic\" не существует!\n\n" +
                    "Запустите скрипт init_database.sql для создания базы данных.\n\n" +
                    "Попытка " + attempts + " из " + maxAttempts;
        }

        return "Не удалось подключиться к базе данных.\n" +
                "Проверьте:\n" +
                "1. Правильность логина и пароля\n" +
                "2. Запущен ли сервер PostgreSQL\n" +
                "3. Существует ли база данных dental_clinic\n\n" +
                "Попытка " + attempts + " из " + maxAttempts;
    }

    @Override
    public void stop() {
        DatabaseConfig.closeConnection();
        logger.info("Application stopped");
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}