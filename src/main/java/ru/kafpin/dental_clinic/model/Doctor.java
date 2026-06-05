package ru.kafpin.dental_clinic.model;

import java.time.LocalDateTime;

public class Doctor {
    private Long doctorId;
    private String fullName;
    private String specialization;
    private String workSchedule;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Doctor() {}

    public Doctor(String fullName, String specialization, String workSchedule) {
        this.fullName = fullName;
        this.specialization = specialization;
        this.workSchedule = workSchedule;
    }

    // Геттеры и сеттеры
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getWorkSchedule() { return workSchedule; }
    public void setWorkSchedule(String workSchedule) { this.workSchedule = workSchedule; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return fullName != null ? fullName : "";
    }
}