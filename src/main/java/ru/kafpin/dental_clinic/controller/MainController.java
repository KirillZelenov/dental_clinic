package ru.kafpin.dental_clinic.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML private TabPane mainTabPane;
    @FXML private Label statusLabel;

    @FXML private javafx.scene.control.Menu fileMenu;
    @FXML private javafx.scene.control.Menu referencesMenu;
    @FXML private javafx.scene.control.Menu helpMenu;
    @FXML private javafx.scene.control.Menu languageMenu;
    @FXML private javafx.scene.control.MenuItem exitMenuItem;
    @FXML private javafx.scene.control.MenuItem aboutMenuItem;
    @FXML private javafx.scene.control.MenuItem doctorsMenuItem;
    @FXML private javafx.scene.control.MenuItem servicesMenuItem;

    private ResourceBundle bundle;
    private Stage primaryStage;

    public MainController() {
        Locale.setDefault(new Locale("ru", "RU"));
        bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("ru", "RU"));
    }

    @FXML
    public void initialize() {
        updateAllTexts();
        setupLanguageMenu();
        loadInitialTabs();

        if (mainTabPane != null && mainTabPane.getScene() != null) {
            primaryStage = (Stage) mainTabPane.getScene().getWindow();
        }
    }

    private void setupLanguageMenu() {
        if (languageMenu != null) {
            languageMenu.getItems().clear();

            javafx.scene.control.MenuItem russianItem = new javafx.scene.control.MenuItem("Русский");
            javafx.scene.control.MenuItem englishItem = new javafx.scene.control.MenuItem("English");
            javafx.scene.control.MenuItem germanItem = new javafx.scene.control.MenuItem("Deutsch");

            russianItem.setOnAction(e -> switchLanguage(new Locale("ru", "RU")));
            englishItem.setOnAction(e -> switchLanguage(new Locale("en", "US")));
            germanItem.setOnAction(e -> switchLanguage(new Locale("de", "DE")));

            languageMenu.getItems().addAll(russianItem, englishItem, germanItem);
        }
    }

    private void switchLanguage(Locale locale) {
        bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", locale);
        updateAllTexts();
        reloadTabs();
        updateAllChildControllers();
    }

    private void updateAllChildControllers() {
        if (mainTabPane == null) return;
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getContent() != null) {
                Object controller = tab.getUserData();
                if (controller instanceof BaseController) {
                    ((BaseController) controller).setBundle(bundle);
                }
            }
        }
    }

    private void updateAllTexts() {
        if (fileMenu != null) fileMenu.setText(getString("menu.file"));
        if (referencesMenu != null) referencesMenu.setText(getString("menu.references"));
        if (helpMenu != null) helpMenu.setText(getString("menu.help"));
        if (languageMenu != null) languageMenu.setText(getString("menu.language"));
        if (exitMenuItem != null) exitMenuItem.setText(getString("menu.exit"));
        if (aboutMenuItem != null) aboutMenuItem.setText(getString("menu.about"));
        if (doctorsMenuItem != null) doctorsMenuItem.setText(getString("menu.doctors"));
        if (servicesMenuItem != null) servicesMenuItem.setText(getString("menu.services"));
        if (statusLabel != null) statusLabel.setText(getString("status.loaded"));

        if (mainTabPane != null && mainTabPane.getScene() != null) {
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            if (stage != null) {
                stage.setTitle(getString("app.title"));
                primaryStage = stage;
            }
        }

        updateTabTitles();
    }

    private String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    private void updateTabTitles() {
        if (mainTabPane == null) return;

        String[] tabTitles = {
                getString("tab.patients"),
                getString("tab.appointment"),
                getString("tab.schedule"),
                getString("tab.medical_history"),
                getString("tab.finance"),
                getString("tab.reports")
        };

        for (int i = 0; i < mainTabPane.getTabs().size() && i < tabTitles.length; i++) {
            mainTabPane.getTabs().get(i).setText(tabTitles[i]);
        }
    }

    private void loadInitialTabs() {
        loadTab(getString("tab.patients"), "/ru/kafpin/dental_clinic/view/patients-view.fxml");
        loadTab(getString("tab.appointment"), "/ru/kafpin/dental_clinic/view/appointment-form.fxml");
        loadTab(getString("tab.schedule"), "/ru/kafpin/dental_clinic/view/schedule-view.fxml");
        loadTab(getString("tab.medical_history"), "/ru/kafpin/dental_clinic/view/medical-history-view.fxml");
        loadTab(getString("tab.finance"), "/ru/kafpin/dental_clinic/view/finance-view.fxml");
        loadTab(getString("tab.reports"), "/ru/kafpin/dental_clinic/view/reports-view.fxml");
    }

    private void reloadTabs() {
        if (mainTabPane == null) return;
        int selectedIndex = mainTabPane.getSelectionModel().getSelectedIndex();
        mainTabPane.getTabs().clear();
        loadInitialTabs();

        if (selectedIndex >= 0 && selectedIndex < mainTabPane.getTabs().size()) {
            mainTabPane.getSelectionModel().select(selectedIndex);
        }
    }

    private void loadTab(String title, String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.error("FXML not found: {}", fxmlPath);
                Label errorLabel = new Label("Module not found: " + fxmlPath);
                errorLabel.setStyle("-fx-padding: 20; -fx-text-fill: red;");
                Tab tab = new Tab(title);
                tab.setContent(errorLabel);
                tab.setClosable(false);
                if (mainTabPane != null) mainTabPane.getTabs().add(tab);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setResources(bundle);
            Parent content = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setBundle(bundle);
            }

            Tab tab = new Tab(title);
            tab.setContent(content);
            tab.setClosable(false);
            tab.setUserData(controller);
            if (mainTabPane != null) mainTabPane.getTabs().add(tab);

        } catch (Exception e) {
            logger.error("Error loading tab: " + fxmlPath, e);
            Label errorLabel = new Label("Error loading: " + e.getMessage());
            errorLabel.setStyle("-fx-padding: 20; -fx-text-fill: red;");
            Tab tab = new Tab(title);
            tab.setContent(errorLabel);
            tab.setClosable(false);
            if (mainTabPane != null) mainTabPane.getTabs().add(tab);
        }
    }

    @FXML
    public void handleExit() {
        logger.info("Exiting application");
        if (mainTabPane != null && mainTabPane.getScene() != null) {
            Stage stage = (Stage) mainTabPane.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        } else if (primaryStage != null) {
            primaryStage.close();
        } else {
            System.exit(0);
        }
    }

    @FXML
    public void showAbout() {
        String content = "═════════════════════════════════════════════════\n" +
                "                                            " + getString("about.course_work") + "\n" +
                "═════════════════════════════════════════════════\n\n" +
                getString("about.developer") + "\n" +
                getString("about.group") + "\n" +
                getString("about.year") + "\n\n" +
                "═════════════════════════════════════════════════";

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(getString("about.title"));
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    @FXML
    public void openDoctors() {
        logger.info("Opening doctors dictionary");
        openDictionary("/ru/kafpin/dental_clinic/view/doctors-view.fxml", getString("menu.doctors"));
    }

    @FXML
    public void openServices() {
        logger.info("Opening services dictionary");
        openDictionary("/ru/kafpin/dental_clinic/view/services-view.fxml", getString("menu.services"));
    }

    private void openDictionary(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                logger.error("FXML not found: {}", fxmlPath);
                showAlert(getString("error.title"), getString("error.fileNotFound") + ": " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setResources(bundle);
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof BaseController) {
                ((BaseController) controller).setBundle(bundle);
            }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, 900, 600));
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            if (mainTabPane != null && mainTabPane.getScene() != null) {
                stage.initOwner(mainTabPane.getScene().getWindow());
            } else if (primaryStage != null) {
                stage.initOwner(primaryStage);
            }

            stage.showAndWait();

        } catch (Exception e) {
            logger.error("Error opening dictionary: " + fxmlPath, e);
            showAlert(getString("error.title"), getString("error.openFailed") + ": " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}