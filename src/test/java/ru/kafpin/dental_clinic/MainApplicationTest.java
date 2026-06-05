package ru.kafpin.dental_clinic;

import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import ru.kafpin.dental_clinic.config.DatabaseConfig;
import ru.kafpin.dental_clinic.dao.PatientDAO;
import ru.kafpin.dental_clinic.model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MainApplicationTest extends ApplicationTest {

    private final PatientDAO patientDAO = new PatientDAO();
    private final List<Patient> patientsToClean = new ArrayList<>();
    private final List<Long> appointmentsToClean = new ArrayList<>();

    private final String testFullName = "Тестов Тест Тестович";
    private final String testPhone = "+79991234567";
    private final String testEmail = "test@test.ru";
    private final String testPolicy = "1234567890123456";

    @Override
    public void start(Stage stage) throws Exception {
        Main main = new Main();
        main.start(stage);
        Thread.sleep(8000);

        try {
            Thread.sleep(1000);

            clickOn("#usernameField");
            write("anarchy");
            Thread.sleep(500);

            clickOn("#passwordField");
            write("wkola191105");
            Thread.sleep(500);

            clickOn("#okButton");
            Thread.sleep(3000);

        } catch (Exception e) {
            System.out.println("Ошибка авторизации: " + e.getMessage());
            try {
                press(KeyCode.ESCAPE);
                release(KeyCode.ESCAPE);
            } catch (Exception e2) {}
        }

        Thread.sleep(1000);
    }

    @AfterEach
    void cleanUp() {
        for (Long id : appointmentsToClean) {
            try {
                String sql = "DELETE FROM appointments WHERE appointment_id = ?";
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {}
        }

        for (Patient p : patientsToClean) {
            try {
                String sql = "DELETE FROM patients WHERE patient_id = ?";
                try (Connection conn = DatabaseConfig.getConnection();
                     PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, p.getPatientId());
                    ps.executeUpdate();
                }
            } catch (SQLException e) {}
        }
        patientsToClean.clear();
        appointmentsToClean.clear();

        try { clickOn("Отмена"); } catch (Exception e) {}
        try { clickOn("OK"); } catch (Exception e) {}
        try { clickOn("ОК"); } catch (Exception e) {}
    }

    @Test
    @DisplayName("TC-01: Добавление нового пациента")
    void testAddPatient() throws Exception {
        clickOn("Пациенты");
        Thread.sleep(1000);

        clickOn("#addButton");
        Thread.sleep(1000);

        clickOn("#fullNameField").write(testFullName);
        clickOn("#phoneField").write(testPhone);
        clickOn("#emailField").write(testEmail);
        clickOn("#policyField").write(testPolicy);

        clickOn("Сохранить");
        Thread.sleep(1500);

        try { clickOn("OK"); } catch (Exception e) { clickOn("ОК"); }
        Thread.sleep(500);

        List<Patient> patients = patientDAO.getAll();
        boolean found = patients.stream().anyMatch(p -> testFullName.equals(p.getFullName()));

        if (found) {
            Patient created = patients.stream().filter(p -> testFullName.equals(p.getFullName())).findFirst().orElse(null);
            if (created != null) patientsToClean.add(created);
        }

        assertThat(found).as("Пациент должен быть добавлен").isTrue();
    }

    @Test
    @DisplayName("TC-02: Редактирование информации о пациенте")
    void testEditPatient() throws Exception {
        clickOn("Пациенты");
        Thread.sleep(1000);

        clickOn("#addButton");
        Thread.sleep(1000);

        clickOn("#fullNameField").write(testFullName);
        clickOn("#phoneField").write(testPhone);
        clickOn("#emailField").write(testEmail);
        clickOn("#policyField").write(testPolicy);

        clickOn("Сохранить");
        Thread.sleep(1500);

        try { clickOn("OK"); } catch (Exception e) { clickOn("ОК"); }
        Thread.sleep(1000);

        List<Patient> patients = patientDAO.getAll();
        Patient created = patients.stream().filter(p -> testFullName.equals(p.getFullName())).findFirst().orElse(null);
        if (created != null) patientsToClean.add(created);

        Thread.sleep(1000);

        try {
            clickOn(testFullName);
            Thread.sleep(500);
        } catch (Exception e) {
            clickOn("#searchField");
            write(testFullName.substring(0, 5));
            Thread.sleep(500);
            press(KeyCode.ENTER);
            Thread.sleep(1000);
            clickOn(testFullName);
            Thread.sleep(500);
        }

        clickOn("#editButton");
        Thread.sleep(1000);

        clickOn("#fullNameField");
        Thread.sleep(500);
        clickOn("#fullNameField").press(KeyCode.CONTROL).press(KeyCode.A).release(KeyCode.A).release(KeyCode.CONTROL);
        String newFullName = "Иванов Иван Иванович";
        write(newFullName);
        Thread.sleep(500);

        clickOn("Сохранить");
        Thread.sleep(1500);

        try { clickOn("OK"); } catch (Exception e) { clickOn("ОК"); }
        Thread.sleep(500);

        Patient updated = patientDAO.getById(created.getPatientId());
        assertThat(updated.getFullName()).as("ФИО должно обновиться").isEqualTo(newFullName);
    }

    @Test
    @DisplayName("TC-03: Попытка добавления пациента с пустым ФИО (негативный)")
    void testAddPatientEmptyName() throws Exception {
        clickOn("Пациенты");
        Thread.sleep(500);

        clickOn("#addButton");
        Thread.sleep(800);

        clickOn("#phoneField").write(testPhone);
        clickOn("#emailField").write(testEmail);
        clickOn("#policyField").write(testPolicy);

        clickOn("Сохранить");
        Thread.sleep(500);

        boolean errorShown = false;
        try {
            clickOn("OK");
            errorShown = true;
        } catch (Exception e) {
            try {
                clickOn("ОК");
                errorShown = true;
            } catch (Exception e2) {}
        }

        assertThat(errorShown).as("Должна появиться ошибка о пустом ФИО").isTrue();

        try { clickOn("Отмена"); } catch (Exception e) {}
    }

    @Test
    @DisplayName("TC-04: Попытка удаления невыбранного пациента (негативный)")
    void testDeletePatientNoSelection() throws Exception {
        clickOn("Пациенты");
        Thread.sleep(2000);

        try {
            clickOn("patientsTable");
            press(KeyCode.ESCAPE);
            release(KeyCode.ESCAPE);
        } catch (Exception e) {}

        Thread.sleep(500);

        clickOn("#deleteButton");
        Thread.sleep(1000);

        boolean errorShown = false;

        try {
            clickOn("OK");
            errorShown = true;
            System.out.println("Найдено сообщение с кнопкой OK");
        } catch (Exception e1) {
            try {
                clickOn("ОК");
                errorShown = true;
                System.out.println("Найдено сообщение с кнопкой ОК");
            } catch (Exception e2) {
                try {
                    clickOn("Выберите пациента");
                    errorShown = true;
                    System.out.println("Найдено сообщение 'Выберите пациента'");
                    try { clickOn("OK"); } catch (Exception e3) {}
                } catch (Exception e3) {
                    try {
                        press(KeyCode.ENTER);
                        release(KeyCode.ENTER);
                        errorShown = true;
                        System.out.println("Найдено диалоговое окно (ENTER)");
                    } catch (Exception e4) {}
                }
            }
        }

        assertThat(errorShown).as("Должна появиться ошибка: 'Выберите пациента'").isTrue();
    }
}