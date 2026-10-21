package com.example.hosptial;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
java.util.List<Appointment> findByDoctorAccountUsername(String username);
java.util.List<Appointment> findByPatientAccountUsername(String username);
boolean existsByPatientIdAndDoctorAccountUsername(Long patientId, String username);
boolean existsByDoctorIdAndDateAndTimeAndStatusNot(Long doctorId, LocalDate date, LocalTime time, AppointmentStatus excludedStatus);
    static void addAppointmemt(Appointment appointment) {

        throw new UnsupportedOperationException("Unimplemented method 'addAppointmemt'");
    }
    
}
