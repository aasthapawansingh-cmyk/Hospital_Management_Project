package com.example.hosptial;

public class DoctorAppointmentResolution {
    public enum Action { REASSIGN, CANCEL }

    @jakarta.validation.constraints.NotNull
    private Long appointmentId;
    @jakarta.validation.constraints.NotNull
    private Action action;
    private Long replacementDoctorId;

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }
    public Long getReplacementDoctorId() { return replacementDoctorId; }
    public void setReplacementDoctorId(Long replacementDoctorId) { this.replacementDoctorId = replacementDoctorId; }
}
