package com.example.hosptial;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

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
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndDateAndTime(2L, date, time)).thenReturn(false);

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(date);
        request.setTime(time);
        request.setStatus(AppointmentStatus.PENDING);

        service.addAppointment(request);

        ArgumentCaptor<Appointment> captor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(captor.capture());

        Appointment savedAppointment = captor.getValue();
        assertEquals(date, savedAppointment.getDate());
        assertEquals(time, savedAppointment.getTime());
        assertEquals(AppointmentStatus.PENDING, savedAppointment.getStatus());
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
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndDateAndTime(2L, date, time)).thenReturn(true);

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(date);
        request.setTime(time);
        request.setStatus(AppointmentStatus.PENDING);

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
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        AppointmentRequestDTO request = new AppointmentRequestDTO();
        request.setPatientId(1L);
        request.setDoctorId(2L);
        request.setDate(LocalDate.of(2026, 5, 1));
        request.setTime(LocalTime.of(10, 0));
        request.setStatus(AppointmentStatus.PENDING);

        DoctorNotFoundException exception = assertThrows(
                DoctorNotFoundException.class,
                () -> service.addAppointment(request));

        assertEquals("Doctor not found", exception.getMessage());
        verify(appointmentRepository, never()).existsByDoctorIdAndDateAndTime(anyLong(), any(), any());
        verify(appointmentRepository, never()).save(any());
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

        Appointment updatedAppointment = service.updateStatus(10L, AppointmentStatus.CONFIRMED);

        assertEquals(AppointmentStatus.CONFIRMED, updatedAppointment.getStatus());
        verify(appointmentRepository).save(appointment);
    }

    @Test
    void shouldUpdateStatusFromConfirmedToCompleted() {
        AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        DoctorRepository doctorRepository = mock(DoctorRepository.class);

        Appointment appointment = new Appointment(10L, LocalDate.of(2026, 5, 1), LocalTime.of(10, 0),
                AppointmentStatus.CONFIRMED);

        when(appointmentRepository.findById(10L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppointmentService service = new AppointmentService(appointmentRepository, patientRepository, doctorRepository);

        Appointment updatedAppointment = service.updateStatus(10L, AppointmentStatus.COMPLETED);

        assertEquals(AppointmentStatus.COMPLETED, updatedAppointment.getStatus());
        verify(appointmentRepository).save(appointment);
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
                () -> service.updateStatus(10L, AppointmentStatus.COMPLETED));

        assertEquals("Invalid status transition", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
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
                () -> service.updateStatus(99L, AppointmentStatus.CONFIRMED));

        assertEquals("Appointment not found", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }
}
