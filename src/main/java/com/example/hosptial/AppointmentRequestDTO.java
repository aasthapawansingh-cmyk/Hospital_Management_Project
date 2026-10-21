package com.example.hosptial;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AppointmentRequestDTO {
    @NotNull private LocalDate date;
    @NotNull private LocalTime time;
    @NotNull private Long patientId;
    @NotNull private Long doctorId;
private AppointmentStatus status;
    @NotBlank @Size(max = 1000) private String reason;

public AppointmentRequestDTO(){

}
public AppointmentRequestDTO(LocalDate date, LocalTime time, Long patientId, Long doctorId, AppointmentStatus status, String reason) {
    this.date = date; this.time = time; this.patientId = patientId; this.doctorId = doctorId; this.status = status; this.reason = reason;
}
public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime(){
        return time;
    }
    public void setTime(LocalTime time){
        this.time=time;
    }
    public AppointmentStatus getStatus(){
        return status;
    }
    public void setStatus(AppointmentStatus status){
        this.status=status;
    }
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

  


}
