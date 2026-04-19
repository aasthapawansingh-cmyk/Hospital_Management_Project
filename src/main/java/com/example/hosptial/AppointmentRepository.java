package com.example.hosptial;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    static void addAppointmemt(Appointment appointment) {

        throw new UnsupportedOperationException("Unimplemented method 'addAppointmemt'");
    }
    
}
