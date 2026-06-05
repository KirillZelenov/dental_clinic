package ru.kafpin.dental_clinic.dto;

import java.math.BigDecimal;

public class DebtDTO {
    private Long appointmentId;
    private String patientName;
    private BigDecimal amount;
    private String paymentStatus;

    public DebtDTO() {}

    // Геттеры и сеттеры
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
}
