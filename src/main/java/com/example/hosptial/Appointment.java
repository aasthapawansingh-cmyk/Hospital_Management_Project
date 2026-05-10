package com.example.hosptial;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Appointment {
    @Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
private LocalDate date;
private LocalTime time;
@Enumerated(EnumType.STRING)
private AppointmentStatus status;

@ManyToOne
private Patient patient;
@ManyToOne
private Doctor doctor;
public Appointment(Long id,LocalDate date,LocalTime time,AppointmentStatus status){
this.id=id;
this.date=date;
this.time=time;
this.status=status;
}
public Appointment(){

}
public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
    public void setStatus( AppointmentStatus status){
        this.status=status;
    }
    public Patient getPatient() { return patient; }
public void setPatient(Patient patient) { this.patient = patient; }

public Doctor getDoctor() { return doctor; }
public void setDoctor(Doctor doctor) { this.doctor = doctor; }



}
