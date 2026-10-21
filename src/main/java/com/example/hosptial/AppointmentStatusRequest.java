package com.example.hosptial;
import jakarta.validation.constraints.NotNull;
public class AppointmentStatusRequest {
    @NotNull
    private AppointmentStatus status;
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
