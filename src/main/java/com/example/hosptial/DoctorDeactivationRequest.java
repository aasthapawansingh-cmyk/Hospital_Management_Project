package com.example.hosptial;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DoctorDeactivationRequest {
    @NotBlank @Size(max = 1000)
    private String reason;
    @Valid
    private List<DoctorAppointmentResolution> resolutions = new ArrayList<>();

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<DoctorAppointmentResolution> getResolutions() { return resolutions; }
    public void setResolutions(List<DoctorAppointmentResolution> resolutions) { this.resolutions = resolutions; }
}
