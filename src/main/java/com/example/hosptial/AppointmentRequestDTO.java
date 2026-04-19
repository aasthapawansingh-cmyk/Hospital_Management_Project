package com.example.hosptial;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentRequestDTO {
    private LocalDate date;
private LocalTime time;
private Long patientId;
private Long doctorId;
private String status;
public AppointmentRequestDTO(){

}
public AppointmentRequestDTO(LocalDate date,LocalTime time,Long patientId,Long doctorId,String status){
this.date=date;
this.time=time;
this.doctorId =doctorId;
this.patientId=patientId;
this.status=status;

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
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status=status;
    }
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

  


}
