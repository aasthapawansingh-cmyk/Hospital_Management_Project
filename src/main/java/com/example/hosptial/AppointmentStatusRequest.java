package com.example.hosptial;
import jakarta.validation.constraints.NotNull;
public class AppointmentStatusRequest {
    @NotNull
    private AppointmentStatus status;
    @jakarta.validation.constraints.Size(max = 1000)
    private String reason;
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
