package com.example.hosptial;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
java.util.List<Appointment> findByDoctorAccountUsername(String username);
java.util.List<Appointment> findByPatientAccountUsername(String username);
java.util.List<Appointment> findByDoctorIdAndStatusInOrderByDateAscTimeAsc(Long doctorId, java.util.Collection<AppointmentStatus> statuses);
@org.springframework.data.jpa.repository.Query("select a from Appointment a where a.doctor.id = :doctorId and a.status in :statuses and (a.date > :date or (a.date = :date and a.time >= :time)) order by a.date, a.time")
java.util.List<Appointment> findUpcomingForDoctor(@org.springframework.data.repository.query.Param("doctorId") Long doctorId,
        @org.springframework.data.repository.query.Param("statuses") java.util.Collection<AppointmentStatus> statuses,
        @org.springframework.data.repository.query.Param("date") LocalDate date,
        @org.springframework.data.repository.query.Param("time") LocalTime time);
boolean existsByPatientIdAndDoctorAccountUsername(Long patientId, String username);
boolean existsByDoctorIdAndDateAndTimeAndStatusNot(Long doctorId, LocalDate date, LocalTime time, AppointmentStatus excludedStatus);
boolean existsByDoctorIdAndDateAndTimeAndStatusNotAndIdNot(Long doctorId, LocalDate date, LocalTime time, AppointmentStatus excludedStatus, Long excludedId);
    static void addAppointmemt(Appointment appointment) {

        throw new UnsupportedOperationException("Unimplemented method 'addAppointmemt'");
    }
    
}
