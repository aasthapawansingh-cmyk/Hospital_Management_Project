package com.example.hosptial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.example.hosptial.auth.AuthUser;
import com.example.hosptial.auth.CustomUserDetailsService;
import com.example.hosptial.auth.Role;
import com.example.hosptial.auth.RoleName;
import com.example.hosptial.auth.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class DoctorLifecycleTest {
    @Test
    void deactivationCancelsAppointmentsDisablesLoginAndAuditsReason() {
        DoctorRepository doctors = mock(DoctorRepository.class);
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        DoctorStatusHistoryRepository history = mock(DoctorStatusHistoryRepository.class);
        UserRepository users = mock(UserRepository.class);
        Doctor doctor = doctor(1L, "dr1");
        Appointment appointment = appointment(10L, doctor);
        AuthUser account = new AuthUser(); account.setUsername("dr1");
        when(doctors.findByIdForUpdate(1L)).thenReturn(Optional.of(doctor));
        when(appointments.findUpcomingForDoctor(eq(1L), anyCollection(), any(LocalDate.class), any(LocalTime.class))).thenReturn(List.of(appointment));
        when(users.findByUsername("dr1")).thenReturn(Optional.of(account));
        DoctorDeactivationRequest request = new DoctorDeactivationRequest(); request.setReason("Staff departure");
        DoctorAppointmentResolution resolution = new DoctorAppointmentResolution(); resolution.setAppointmentId(10L); resolution.setAction(DoctorAppointmentResolution.Action.CANCEL);
        request.setResolutions(List.of(resolution));

        Doctor result = new DoctorService(doctors, appointments, history, users).deactivate(1L, request, "admin");

        assertFalse(result.isActive()); assertFalse(account.isEnabled()); assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals("admin", appointment.getStatusHistory().get(0).getActorUsername());
        assertEquals("Staff departure", appointment.getStatusHistory().get(0).getReason());
        verify(history).save(argThat(h -> !h.isActive() && h.getReason().equals("Staff departure")));
    }

    @Test
    void deactivationRequiresResolvingEveryFutureAppointment() {
        DoctorRepository doctors = mock(DoctorRepository.class); AppointmentRepository appointments = mock(AppointmentRepository.class);
        when(doctors.findByIdForUpdate(1L)).thenReturn(Optional.of(doctor(1L, null)));
        when(appointments.findUpcomingForDoctor(eq(1L), anyCollection(), any(LocalDate.class), any(LocalTime.class))).thenReturn(List.of(appointment(10L, doctor(1L, null))));
        DoctorDeactivationRequest request = new DoctorDeactivationRequest(); request.setReason("Staff departure");
        assertThrows(IllegalArgumentException.class, () -> new DoctorService(doctors, appointments, mock(DoctorStatusHistoryRepository.class), mock(UserRepository.class)).deactivate(1L, request, "admin"));
    }

    @Test
    void reassignmentRejectsBookedReplacementAndPreservesOldAssignment() {
        DoctorRepository doctors = mock(DoctorRepository.class); AppointmentRepository appointments = mock(AppointmentRepository.class);
        Doctor oldDoctor = doctor(1L, null), replacement = doctor(2L, null); Appointment appointment = appointment(10L, oldDoctor);
        when(doctors.findByIdForUpdate(1L)).thenReturn(Optional.of(oldDoctor)); when(doctors.findByIdForUpdate(2L)).thenReturn(Optional.of(replacement));
        when(appointments.findUpcomingForDoctor(eq(1L), anyCollection(), any(LocalDate.class), any(LocalTime.class))).thenReturn(List.of(appointment));
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNot(eq(2L), any(), any(), eq(AppointmentStatus.CANCELLED))).thenReturn(true);
        DoctorDeactivationRequest request = new DoctorDeactivationRequest(); request.setReason("Leave");
        DoctorAppointmentResolution resolution = new DoctorAppointmentResolution(); resolution.setAppointmentId(10L); resolution.setAction(DoctorAppointmentResolution.Action.REASSIGN); resolution.setReplacementDoctorId(2L); request.setResolutions(List.of(resolution));
        assertThrows(AppointmentAlreadyBookedException.class, () -> new DoctorService(doctors, appointments, mock(DoctorStatusHistoryRepository.class), mock(UserRepository.class)).deactivate(1L, request, "admin"));
        assertSame(oldDoctor, appointment.getDoctor()); verify(appointments, never()).save(appointment);
    }

    @Test
    void deactivationReassignsToActiveDoctorAndAuditsBothAssignments() {
        DoctorRepository doctors = mock(DoctorRepository.class); AppointmentRepository appointments = mock(AppointmentRepository.class);
        Doctor oldDoctor = doctor(1L, null), replacement = doctor(2L, null); Appointment appointment = appointment(10L, oldDoctor);
        when(doctors.findByIdForUpdate(1L)).thenReturn(Optional.of(oldDoctor)); when(doctors.findByIdForUpdate(2L)).thenReturn(Optional.of(replacement));
        when(appointments.findUpcomingForDoctor(eq(1L), anyCollection(), any(LocalDate.class), any(LocalTime.class))).thenReturn(List.of(appointment));
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNot(eq(2L), any(), any(), eq(AppointmentStatus.CANCELLED))).thenReturn(false);
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        DoctorAppointmentResolution resolution = new DoctorAppointmentResolution(); resolution.setAppointmentId(10L); resolution.setAction(DoctorAppointmentResolution.Action.REASSIGN); resolution.setReplacementDoctorId(2L);
        DoctorDeactivationRequest request = new DoctorDeactivationRequest(); request.setReason("Leave"); request.setResolutions(List.of(resolution));
        new DoctorService(doctors, appointments, mock(DoctorStatusHistoryRepository.class), mock(UserRepository.class)).deactivate(1L, request, "admin");
        assertSame(replacement, appointment.getDoctor()); assertEquals(1, appointment.getAssignmentHistory().size());
        assertSame(oldDoctor, appointment.getAssignmentHistory().get(0).getFromDoctor());
        assertSame(replacement, appointment.getAssignmentHistory().get(0).getToDoctor());
        assertEquals("Leave", appointment.getAssignmentHistory().get(0).getReason());
    }

    @Test
    void disabledDoctorAccountCannotAuthenticateOrUseAnExistingToken() {
        UserRepository users = mock(UserRepository.class); DoctorRepository doctors = mock(DoctorRepository.class);
        AuthUser account = new AuthUser("doctor", "encoded", java.util.Set.of(new Role(1L, RoleName.DOCTOR)));
        account.setEnabled(false); when(users.findByUsername("doctor")).thenReturn(Optional.of(account));
        var details = new CustomUserDetailsService(users, doctors).loadUserByUsername("doctor");
        assertFalse(details.isEnabled());
    }

    @Test
    void doctorLifecycleEndpointsAreAdminOnly() throws Exception {
        var deactivate = DoctorController.class.getMethod("deactivate", Long.class, DoctorDeactivationRequest.class, org.springframework.security.core.userdetails.UserDetails.class);
        var activate = DoctorController.class.getMethod("activate", Long.class, DoctorActivationRequest.class, org.springframework.security.core.userdetails.UserDetails.class);
        assertEquals("hasRole('ADMIN')", deactivate.getAnnotation(PreAuthorize.class).value());
        assertEquals("hasRole('ADMIN')", activate.getAnnotation(PreAuthorize.class).value());
    }

    private static Doctor doctor(Long id, String username) { Doctor d = new Doctor(id, "Doctor", "General", "000"); d.setAccountUsername(username); return d; }
    private static Appointment appointment(Long id, Doctor doctor) { Appointment a = new Appointment(id, LocalDate.now().plusDays(2), LocalTime.NOON, AppointmentStatus.CONFIRMED); a.setDoctor(doctor); a.setPatient(new Patient(2L, "Patient", 30, "X", "000", "Address")); return a; }
}
