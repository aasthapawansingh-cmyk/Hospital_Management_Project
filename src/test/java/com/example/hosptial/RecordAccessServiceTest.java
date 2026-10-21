package com.example.hosptial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import com.example.hosptial.auth.*;

class RecordAccessServiceTest {
    @Test
    void patientCanReadOnlyOwnAppointmentAndAssignedDoctorCanReadIt() {
        UserRepository users = mock(UserRepository.class);
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());
        RecordAccessService access = new RecordAccessService(users);
        Patient patient = new Patient(1L,"P",32,"F","1234567890","A"); patient.setAccountUsername("patient-a");
        Doctor doctor = new Doctor(2L,"D","General","1234567890"); doctor.setAccountUsername("doctor-a");
        Appointment appointment = new Appointment(3L,null,null,AppointmentStatus.PENDING); appointment.setPatient(patient); appointment.setDoctor(doctor);

        assertDoesNotThrow(() -> access.requireAppointmentAccess("patient-a", appointment));
        assertDoesNotThrow(() -> access.requireAppointmentAccess("doctor-a", appointment));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> access.requireAppointmentAccess("patient-b", appointment));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> access.requireAppointmentAccess("doctor-b", appointment));
    }

    @Test
    void administratorAccessComesFromServerSideRoleRecord() {
        UserRepository users = mock(UserRepository.class);
        Role adminRole = new Role(1L, RoleName.ADMIN);
        when(users.findByUsername("admin-user")).thenReturn(Optional.of(new AuthUser("admin-user","hash", Set.of(adminRole))));
        RecordAccessService access = new RecordAccessService(users);
        Appointment appointment = new Appointment(9L,null,null,AppointmentStatus.PENDING);
        assertDoesNotThrow(() -> access.requireAppointmentAccess("admin-user", appointment));
    }
}
