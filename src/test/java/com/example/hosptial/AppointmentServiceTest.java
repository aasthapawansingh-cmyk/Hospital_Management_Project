package com.example.hosptial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import com.example.hosptial.auth.RoleName;

public class AppointmentServiceTest {

    @Test
    void shouldSaveAppointmentWhenSlotIsFree() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        Patient patient = new Patient(1L, "Alice", 30, "F", "9999999999", "Delhi");
        Doctor doctor = new Doctor(2L, "Dr. Bob", "Cardiology", "8888888888");

        LocalDate date = LocalDate.of(2026, 5, 1);
        LocalTime time = LocalTime.of(10, 0);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNot(2L, date, time, AppointmentStatus.CANCELLED)).thenReturn(false);

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(date);
        request.setTime(time);
        request.setStatus(AppointmentStatus.PENDING);
        request.setReason("Routine checkup");

        service.addAppointment(request);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        Appointment savedAppointment = captor.getValue();
        assertEquals(date, savedAppointment.getDate());
        assertEquals(time, savedAppointment.getTime());
        assertEquals(AppointmentStatus.PENDING, savedAppointment.getStatus());
        assertEquals("Routine checkup", savedAppointment.getReason());
        assertSame(patient, savedAppointment.getPatient());
        assertSame(doctor, savedAppointment.getDoctor());
    }

    @Test
    void shouldThrowWhenDoctorSlotIsTaken() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        Patient patient = new Patient(1L, "Alice", 30, "F", "9999999999", "Delhi");
        Doctor doctor = new Doctor(2L, "Dr. Bob", "Cardiology", "8888888888");

        LocalDate date = LocalDate.of(2026, 5, 1);
        LocalTime time = LocalTime.of(10, 0);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNot(2L, date, time, AppointmentStatus.CANCELLED)).thenReturn(true);

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(date);
        request.setTime(time);
        request.setStatus(AppointmentStatus.PENDING);
        request.setReason("Routine checkup");

        AppointmentAlreadyBookedException exception = assertThrows(
                AppointmentAlreadyBookedException.class,
                () -> service.addAppointment(request));
        assertEquals("Doctor already has an appointment at this time", exception.getMessage());

        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPatientIsNotFound() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(LocalDate.of(2026, 5, 1));
        request.setTime(LocalTime.of(10, 0));
        request.setStatus(AppointmentStatus.PENDING);
        request.setReason("Routine checkup");

        PatientNotFoundException exception = assertThrows(
                PatientNotFoundException.class,
                () -> service.addAppointment(request));

        assertEquals("Patient not found", exception.getMessage());
        verify(doctorRepository, never()).findById(anyLong());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenDoctorIsNotFound() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        Patient patient = new Patient(1L, "Alice", 30, "F", "9999999999", "Delhi");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(LocalDate.of(2026, 5, 1));
        request.setTime(LocalTime.of(10, 0));
        request.setStatus(AppointmentStatus.PENDING);
        request.setReason("Routine checkup");

        DoctorNotFoundException exception = assertThrows(
                DoctorNotFoundException.class,
                () -> service.addAppointment(request));

        assertEquals("Doctor not found", exception.getMessage());
        verify(appointmentRepository, never()).existsByDoctorIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), any());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void patientRequestUsesTheAuthenticatedPatientsLinkedProfile() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        PatientRepository patients = mock(PatientRepository.class);
        DoctorRepository doctors = mock(DoctorRepository.class);
        Patient patient = new Patient(7L, "Alice", 30, "F", "9999999999", "Delhi");
        patient.setAccountUsername("alice");
        Doctor doctor = new Doctor(2L, "Dr. Bob", "Cardiology", "8888888888");
        when(patients.findByAccountUsernameAndArchivedFalse("alice")).thenReturn(Optional.of(patient));
        when(doctors.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNot(anyLong(), any(), any(), eq(AppointmentStatus.CANCELLED))).thenReturn(false);
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PatientAppointmentRequestDTO request = new PatientAppointmentRequestDTO();
        request.setDoctorId(2L); request.setDate(LocalDate.of(2027, 5, 1)); request.setTime(LocalTime.of(10, 30)); request.setReason("Follow-up");
        Appointment saved = new AppointmentService(appointments, patients, doctors).requestForPatient(request, "alice");
        assertSame(patient, saved.getPatient());
        assertEquals("Follow-up", saved.getReason());
        assertEquals(AppointmentStatus.PENDING, saved.getStatus());
        verify(patients, never()).findById(anyLong());
    }

    @Test
    void availabilityIncludesDoctorsWithFreeSlotsAndExcludesDoctorsWithPendingRequests() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        PatientRepository patients = mock(PatientRepository.class);
        DoctorRepository doctors = mock(DoctorRepository.class);
        Doctor available = new Doctor(2L, "Dr. Free", "Cardiology", "8888888888");
        Doctor busy = new Doctor(3L, "Dr. Busy", "Neurology", "7777777777");
        LocalDate date = LocalDate.of(2027, 5, 1);
        LocalTime time = LocalTime.of(10, 30);
        when(doctors.findByActiveTrue()).thenReturn(java.util.List.of(available, busy));
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNot(2L, date, time, AppointmentStatus.CANCELLED)).thenReturn(false);
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNot(3L, date, time, AppointmentStatus.CANCELLED)).thenReturn(true);

        var result = new AppointmentService(appointments, patients, doctors).getAvailableDoctors(date, time);

        assertEquals(java.util.List.of(available), result);
        verify(appointments).existsByDoctorIdAndDateAndTimeAndStatusNot(3L, date, time, AppointmentStatus.CANCELLED);
    }

    @Test
    void patientRequestRejectsSlotAlreadyTakenByPendingAppointment() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        PatientRepository patients = mock(PatientRepository.class);
        DoctorRepository doctors = mock(DoctorRepository.class);
        Patient patient = new Patient(7L, "Alice", 30, "F", "9999999999", "Delhi");
        Doctor doctor = new Doctor(2L, "Dr. Bob", "Cardiology", "8888888888");
        LocalDate date = LocalDate.of(2027, 5, 1);
        LocalTime time = LocalTime.of(10, 30);
        when(patients.findByAccountUsernameAndArchivedFalse("alice")).thenReturn(Optional.of(patient));
        when(doctors.findByIdForUpdate(2L)).thenReturn(Optional.of(doctor));
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNot(2L, date, time, AppointmentStatus.CANCELLED)).thenReturn(true);
        PatientAppointmentRequestDTO request = new PatientAppointmentRequestDTO();
        request.setDoctorId(2L); request.setDate(date); request.setTime(time); request.setReason("Follow-up");

        AppointmentAlreadyBookedException exception = assertThrows(AppointmentAlreadyBookedException.class,
                () -> new AppointmentService(appointments, patients, doctors).requestForPatient(request, "alice"));

        assertEquals("Doctor already has an appointment at this time", exception.getMessage());
        verify(doctors).findByIdForUpdate(2L);
        verify(appointments, never()).save(any());
    }

    @Test
    void shouldUpdateStatusFromPendingToConfirmed() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        Appointment appointment = new Appointment(10L, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0),
                AppointmentStatus.PENDING);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        Appointment updatedAppointment = service.updateStatus(10L, AppointmentStatus.CONFIRMED, "admin", RoleName.ADMIN, false, false);

        assertEquals(AppointmentStatus.CONFIRMED, updatedAppointment.getStatus());
        assertEquals(1, updatedAppointment.getStatusHistory().size());
        assertEquals("admin", updatedAppointment.getStatusHistory().get(0).getActorUsername());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void assignedDoctorCanConfirmOrRejectTheirPendingRequest() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        Appointment appointment = new Appointment(12L, LocalDate.now().plusDays(1), LocalTime.NOON, AppointmentStatus.PENDING);
        when(appointments.findById(12L)).thenReturn(Optional.of(appointment));
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AppointmentService service = new AppointmentService(appointments, mock(PatientRepository.class), mock(DoctorRepository.class));

        Appointment confirmed = service.updateStatus(12L, AppointmentStatus.CONFIRMED, "doctor", RoleName.DOCTOR, true, false);

        assertEquals(AppointmentStatus.CONFIRMED, confirmed.getStatus());
        assertEquals("doctor", confirmed.getStatusHistory().get(0).getActorUsername());
    }

    @Test
    void shouldUpdateStatusFromConfirmedToInProgress() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        Appointment appointment = new Appointment(10L, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0),
                AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        Appointment updatedAppointment = service.updateStatus(10L, AppointmentStatus.IN_PROGRESS, "doctor", RoleName.DOCTOR, true, false);

        assertEquals(AppointmentStatus.IN_PROGRESS, updatedAppointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void assignedDoctorCanCompleteAnInProgressAppointment() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        Appointment appointment = new Appointment(10L, LocalDate.now(), LocalTime.NOON, AppointmentStatus.IN_PROGRESS);
        when(appointments.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AppointmentService service = new AppointmentService(appointments, mock(PatientRepository.class), mock(DoctorRepository.class));
        Appointment result = service.updateStatus(10L, AppointmentStatus.COMPLETED, "doctor", RoleName.DOCTOR, true, false);
        assertEquals(AppointmentStatus.COMPLETED, result.getStatus());
        assertEquals("doctor", result.getStatusHistory().get(0).getActorUsername());
    }

    @Test
    void patientCanCancelOnlyTheirPendingRequest() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        Appointment appointment = new Appointment(10L, LocalDate.now(), LocalTime.NOON, AppointmentStatus.PENDING);
        when(appointments.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AppointmentService service = new AppointmentService(appointments, mock(PatientRepository.class), mock(DoctorRepository.class));
        assertEquals(AppointmentStatus.CANCELLED, service.updateStatus(10L, AppointmentStatus.CANCELLED,
                "patient", RoleName.PATIENT, false, true).getStatus());
        verify(appointments).save(appointment);
    }

    @Test
    void shouldThrowWhenStatusTransitionIsInvalid() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        Appointment appointment = new Appointment(10L, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0),
                AppointmentStatus.PENDING);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        InvalidAppointmentStatusException exception = assertThrows(
                InvalidAppointmentStatusException.class,
                () -> service.updateStatus(10L, AppointmentStatus.COMPLETED, "doctor", RoleName.DOCTOR, true, false));

        assertEquals("Invalid status transition for this role", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void shouldRejectStatusChangeByUnassignedDoctor() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        Appointment appointment = new Appointment(10L, LocalDate.now(), LocalTime.NOON, AppointmentStatus.PENDING);
        when(appointments.findById(10L)).thenReturn(Optional.of(appointment));
        AppointmentService service = new AppointmentService(appointments, mock(PatientRepository.class), mock(DoctorRepository.class));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> service.updateStatus(10L, AppointmentStatus.IN_PROGRESS, "other-doctor", RoleName.DOCTOR, false, false));
        verify(appointments, never()).save(any());
    }

    @Test
    void shouldRejectSkippingInProgressAndStartingAtCompleted() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        Appointment appointment = new Appointment(10L, LocalDate.now(), LocalTime.NOON, AppointmentStatus.PENDING);
        when(appointments.findById(10L)).thenReturn(Optional.of(appointment));
        AppointmentService service = new AppointmentService(appointments, mock(PatientRepository.class), mock(DoctorRepository.class));
        assertThrows(InvalidAppointmentStatusException.class,
                () -> service.updateStatus(10L, AppointmentStatus.COMPLETED, "admin", RoleName.ADMIN, false, false));
        verify(appointments, never()).save(any());
    }

    @Test
    void adminMayCorrectClinicalProgressOnlyWithAuditedReason() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        Appointment appointment = new Appointment(44L, LocalDate.now(), LocalTime.NOON, AppointmentStatus.CONFIRMED);
        when(appointments.findById(44L)).thenReturn(Optional.of(appointment));
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AppointmentService service = new AppointmentService(appointments, mock(PatientRepository.class), mock(DoctorRepository.class));
        assertThrows(InvalidAppointmentStatusException.class, () -> service.updateStatus(44L, AppointmentStatus.IN_PROGRESS, "admin", RoleName.ADMIN, false, false));
        Appointment corrected = service.updateStatus(44L, AppointmentStatus.IN_PROGRESS, "admin", RoleName.ADMIN, false, false, "Corrected after chart review");
        assertEquals(AppointmentStatus.IN_PROGRESS, corrected.getStatus());
        assertEquals("Corrected after chart review", corrected.getStatusHistory().get(0).getReason());
    }

    @Test
    void adminReschedulesWithSlotRecheckAndScheduleAudit() {
        AppointmentRepository appointments = mock(AppointmentRepository.class);
        DoctorRepository doctors = mock(DoctorRepository.class);
        Doctor previous = new Doctor(2L, "Old", "Cardiology", "111");
        Doctor replacement = new Doctor(3L, "New", "Cardiology", "222");
        Appointment appointment = new Appointment(45L, LocalDate.of(2027, 5, 1), LocalTime.of(10, 0), AppointmentStatus.CONFIRMED);
        appointment.setDoctor(previous);
        when(appointments.findById(45L)).thenReturn(Optional.of(appointment));
        when(doctors.findByIdForUpdate(3L)).thenReturn(Optional.of(replacement));
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNotAndIdNot(3L, LocalDate.of(2027, 5, 2), LocalTime.NOON, AppointmentStatus.CANCELLED, 45L)).thenReturn(false);
        when(appointments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest(); request.setDoctorId(3L); request.setDate(LocalDate.of(2027, 5, 2)); request.setTime(LocalTime.NOON); request.setReason("Clinician unavailable");
        Appointment updated = new AppointmentService(appointments, mock(PatientRepository.class), doctors).reschedule(45L, request, "admin");
        assertEquals(replacement, updated.getDoctor()); assertEquals(LocalDate.of(2027, 5, 2), updated.getDate());
        assertEquals("Clinician unavailable", updated.getScheduleHistory().get(0).getReason());
        assertEquals("admin", updated.getScheduleHistory().get(0).getActorUsername());
    }

    @Test
    void adminRescheduleRejectsTakenSlot() {
        AppointmentRepository appointments = mock(AppointmentRepository.class); DoctorRepository doctors = mock(DoctorRepository.class);
        Appointment appointment = new Appointment(46L, LocalDate.now().plusDays(1), LocalTime.NOON, AppointmentStatus.PENDING);
        when(appointments.findById(46L)).thenReturn(Optional.of(appointment));
        when(doctors.findByIdForUpdate(3L)).thenReturn(Optional.of(new Doctor(3L, "New", "General", "222")));
        when(appointments.existsByDoctorIdAndDateAndTimeAndStatusNotAndIdNot(anyLong(), any(), any(), any(), eq(46L))).thenReturn(true);
        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest(); request.setDoctorId(3L); request.setDate(LocalDate.now().plusDays(2)); request.setTime(LocalTime.NOON); request.setReason("Clinic change");
        assertThrows(AppointmentAlreadyBookedException.class, () -> new AppointmentService(appointments, mock(PatientRepository.class), doctors).reschedule(46L, request, "admin"));
        assertTrue(appointment.getScheduleHistory().isEmpty()); verify(appointments, never()).save(any());
    }

    @Test
    void shouldThrowWhenUpdatingStatusForMissingAppointment() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentNotFoundException exception = assertThrows(
                AppointmentNotFoundException.class,
                () -> service.updateStatus(99L, AppointmentStatus.CONFIRMED, "admin", RoleName.ADMIN, false, false));

        assertEquals("Appointment not found", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }
}
