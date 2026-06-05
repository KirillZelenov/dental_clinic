package ru.kafpin.dental_clinic.controller;

import javafx.scene.control.*;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class BaseController {
    protected ResourceBundle bundle;

    public BaseController() {
        bundle = ResourceBundle.getBundle("ru.kafpin.dental_clinic.i18n.messages", new Locale("ru", "RU"));
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
        updateTexts();
    }

    public abstract void updateTexts();

    protected void updateButton(Button button, String key) {
        if (button != null) {
            try {
                button.setText(bundle.getString(key));
            } catch (Exception e) {
                button.setText(key);
            }
        }
    }

    protected void updateTextFieldPrompt(TextField textField, String key) {
        if (textField != null) {
            try {
                textField.setPromptText(bundle.getString(key));
            } catch (Exception e) {
                textField.setPromptText(key);
            }
        }
    }

    protected void updateComboBoxPrompt(ComboBox<?> comboBox, String key) {
        if (comboBox != null) {
            try {
                comboBox.setPromptText(bundle.getString(key));
            } catch (Exception e) {
                comboBox.setPromptText(key);
            }
        }
    }

    protected void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
}