package ru.kafpin.dental_clinic.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Service {
    private Long serviceId;
    private String serviceName;
    private BigDecimal cost;
    private Integer avgDurationMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Service() {}

    public Service(String serviceName, BigDecimal cost, Integer avgDurationMinutes) {
        this.serviceName = serviceName;
        this.cost = cost;
        this.avgDurationMinutes = avgDurationMinutes;
    }

    // Геттеры и сеттеры
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }

    public Integer getAvgDurationMinutes() { return avgDurationMinutes; }
    public void setAvgDurationMinutes(Integer avgDurationMinutes) { this.avgDurationMinutes = avgDurationMinutes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return serviceName != null ? serviceName + " (" + cost + " руб.)" : "";
    }
}