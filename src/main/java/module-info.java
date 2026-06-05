module pin124.dental_clinic {
    exports ru.kafpin.dental_clinic;
    exports ru.kafpin.dental_clinic.controller;
    exports ru.kafpin.dental_clinic.model;
    exports ru.kafpin.dental_clinic.dao;
    exports ru.kafpin.dental_clinic.dto;
    exports ru.kafpin.dental_clinic.config;

    opens ru.kafpin.dental_clinic.model to javafx.base;
    opens ru.kafpin.dental_clinic.dto to javafx.base;

    opens ru.kafpin.dental_clinic.controller to javafx.fxml;
    exports ru.kafpin.dental_clinic.view;
    opens ru.kafpin.dental_clinic.view to javafx.fxml;

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires org.slf4j;
    requires java.prefs;
}