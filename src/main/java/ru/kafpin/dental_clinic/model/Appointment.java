package ru.kafpin.dental_clinic.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Appointment {
    private Long appointmentId;
    private Long patientId;
    private Long doctorId;
    private Long serviceId;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String status;
    private String reminderSent; // Да, Нет
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Appointment() {}

    public Appointment(Long patientId, Long doctorId, Long serviceId,
                       LocalDate appointmentDate, LocalTime appointmentTime,
                       String status, String reminderSent) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.serviceId = serviceId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = status;
        this.reminderSent = reminderSent;
    }

    // Геттеры и сеттеры
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public LocalDate getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDate appointmentDate) { this.appointmentDate = appointmentDate; }

    public LocalTime getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(LocalTime appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReminderSent() { return reminderSent; }
    public void setReminderSent(String reminderSent) { this.reminderSent = reminderSent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}