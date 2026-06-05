package ru.kafpin.dental_clinic.dto;

public class ScheduleItemDTO {
    private Long appointmentId;
    private Long patientId;
    private String appointmentTime;
    private String doctorName;
    private String patientName;
    private String serviceName;
    private String status;
    private Boolean reminderSent;

    public ScheduleItemDTO() {}

    // Геттеры и сеттеры
    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public Long getPatientId() { return patientId; }  // ДОБАВИТЬ
    public void setPatientId(Long patientId) { this.patientId = patientId; }  // ДОБАВИТЬ

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getReminderSent() { return reminderSent; }
    public void setReminderSent(Boolean reminderSent) { this.reminderSent = reminderSent; }
}