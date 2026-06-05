package ru.kafpin.dental_clinic.model;

import java.time.LocalDateTime;

public class TreatmentRecord {
    private Long treatmentId;
    private Long appointmentId;
    private String toothStatus;
    private String performedWork;
    private String prescriptions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TreatmentRecord() {}

    public TreatmentRecord(Long appointmentId, String toothStatus, String performedWork, String prescriptions) {
        this.appointmentId = appointmentId;
        this.toothStatus = toothStatus;
        this.performedWork = performedWork;
        this.prescriptions = prescriptions;
    }

    // Геттеры и сеттеры
    public Long getTreatmentId() { return treatmentId; }
    public void setTreatmentId(Long treatmentId) { this.treatmentId = treatmentId; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getToothStatus() { return toothStatus; }
    public void setToothStatus(String toothStatus) { this.toothStatus = toothStatus; }

    public String getPerformedWork() { return performedWork; }
    public void setPerformedWork(String performedWork) { this.performedWork = performedWork; }

    public String getPrescriptions() { return prescriptions; }
    public void setPrescriptions(String prescriptions) { this.prescriptions = prescriptions; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
