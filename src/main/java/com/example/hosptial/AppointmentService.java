package com.example.hosptial;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.hosptial.auth.RoleName;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository, DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }
    public void addAppointment(Appointment appointment) {
        if (appointment.getStatus() != null && appointment.getStatus() != AppointmentStatus.PENDING)
            throw new InvalidAppointmentStatusException("New appointments must start as PENDING");
        appointment.setStatus(AppointmentStatus.PENDING);
        appointmentRepository.save(appointment);
    }
    public List<Appointment> getAllAppointments() { return appointmentRepository.findAll(); }
    public Optional<Appointment> getAppointmentById(Long id) { return appointmentRepository.findById(id); }

    public List<Doctor> getAvailableDoctors(LocalDate date, LocalTime time) {
        return doctorRepository.findByActiveTrue().stream()
                .filter(doctor -> !appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNot(
                        doctor.getId(), date, time, AppointmentStatus.CANCELLED))
                .toList();
    }

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus target, String actor, RoleName role,
                                    boolean assignedDoctor, boolean ownPatientAppointment) {
        return updateStatus(id, target, actor, role, assignedDoctor, ownPatientAppointment, null);
    }

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus target, String actor, RoleName role,
                                    boolean assignedDoctor, boolean ownPatientAppointment, String reason) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
        AppointmentStatus current = appointment.getStatus();
        boolean allowed = switch (role) {
            case ADMIN -> (current == AppointmentStatus.PENDING && (target == AppointmentStatus.CONFIRMED || target == AppointmentStatus.CANCELLED))
                    || (current == AppointmentStatus.CONFIRMED && target == AppointmentStatus.CANCELLED)
                    || ((current == AppointmentStatus.CONFIRMED && target == AppointmentStatus.IN_PROGRESS)
                    || (current == AppointmentStatus.IN_PROGRESS && target == AppointmentStatus.COMPLETED)) && reason != null && !reason.isBlank();
            case DOCTOR -> assignedDoctor && ((current == AppointmentStatus.PENDING
                    && (target == AppointmentStatus.CONFIRMED || target == AppointmentStatus.CANCELLED))
                    || (current == AppointmentStatus.CONFIRMED && target == AppointmentStatus.IN_PROGRESS)
                    || (current == AppointmentStatus.IN_PROGRESS && target == AppointmentStatus.COMPLETED));
            case PATIENT -> ownPatientAppointment && current == AppointmentStatus.PENDING && target == AppointmentStatus.CANCELLED;
        };
        if (!allowed) {
            if ((role == RoleName.DOCTOR && !assignedDoctor) || (role == RoleName.PATIENT && !ownPatientAppointment))
                throw new AccessDeniedException("You are not allowed to change this appointment");
            throw new InvalidAppointmentStatusException("Invalid status transition for this role");
        }
        if (target == AppointmentStatus.CANCELLED && (role == RoleName.ADMIN || role == RoleName.DOCTOR) && (reason == null || reason.isBlank()))
            throw new InvalidAppointmentStatusException("A reason is required to cancel an appointment");
        appointment.addStatusHistory(new AppointmentStatusHistory(appointment, current, target, actor, reason == null ? null : reason.trim()));
        appointment.setStatus(target);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment requestForPatient(PatientAppointmentRequestDTO request, String username) {
        Patient patient = patientRepository.findByAccountUsernameAndArchivedFalse(username)
                .orElseThrow(() -> new PatientNotFoundException("Patient profile is not linked to this account"));
        Doctor doctor = doctorRepository.findByIdForUpdate(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        if (!doctor.isActive()) throw new DoctorNotFoundException("Doctor is inactive and cannot receive appointments");
        requireAvailable(doctor.getId(), request.getDate(), request.getTime());
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setReason(request.getReason().trim());
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment addAppointment(AppointmentRequestDTO request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
        Doctor doctor = doctorRepository.findByIdForUpdate(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        if (!doctor.isActive()) throw new DoctorNotFoundException("Doctor is inactive and cannot receive appointments");
        if (request.getStatus() != null && request.getStatus() != AppointmentStatus.PENDING)
            throw new InvalidAppointmentStatusException("New appointments must start as PENDING");
        requireAvailable(doctor.getId(), request.getDate(), request.getTime());
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setDate(request.getDate());
        appointment.setTime(request.getTime());
        appointment.setReason(request.getReason().trim());
        appointment.setStatus(AppointmentStatus.PENDING);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment reschedule(Long appointmentId, AppointmentRescheduleRequest request, String actor) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
        if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED)
            throw new InvalidAppointmentStatusException("Only pending or confirmed appointments can be rescheduled");
        Doctor replacement = doctorRepository.findByIdForUpdate(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        if (!replacement.isActive()) throw new DoctorNotFoundException("Inactive doctors cannot receive appointments");
        if (appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNotAndIdNot(replacement.getId(), request.getDate(), request.getTime(), AppointmentStatus.CANCELLED, appointmentId))
            throw new AppointmentAlreadyBookedException("Doctor already has an appointment at this time");
        appointment.addScheduleHistory(new AppointmentScheduleHistory(appointment, appointment.getDoctor(), replacement,
                appointment.getDate(), appointment.getTime(), request.getDate(), request.getTime(), actor, request.getReason().trim()));
        appointment.setDoctor(replacement); appointment.setDate(request.getDate()); appointment.setTime(request.getTime());
        return appointmentRepository.save(appointment);
    }

    private void requireAvailable(Long doctorId, java.time.LocalDate date, java.time.LocalTime time) {
        if (appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNot(doctorId, date, time, AppointmentStatus.CANCELLED))
            throw new AppointmentAlreadyBookedException("Doctor already has an appointment at this time");
    }
}
