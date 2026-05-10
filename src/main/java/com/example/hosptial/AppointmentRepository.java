package com.example.hosptial;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
boolean existsByDoctorIdAndDateAndTime(Long doctorId, LocalDate date, LocalTime time);
    static void addAppointmemt(Appointment appointment) {

        throw new UnsupportedOperationException("Unimplemented method 'addAppointmemt'");
    }
    
}
