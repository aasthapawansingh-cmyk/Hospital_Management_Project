package com.example.hosptial;

import java.util.List;
import java.util.Optional;
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

    @Transactional
    public Appointment updateStatus(Long id, AppointmentStatus target, String actor, RoleName role,
                                    boolean assignedDoctor, boolean ownPatientAppointment) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
        AppointmentStatus current = appointment.getStatus();
        boolean allowed = switch (role) {
            case ADMIN -> (current == AppointmentStatus.PENDING && (target == AppointmentStatus.CONFIRMED || target == AppointmentStatus.CANCELLED))
                    || (current == AppointmentStatus.CONFIRMED && target == AppointmentStatus.CANCELLED);
            case DOCTOR -> assignedDoctor && ((current == AppointmentStatus.CONFIRMED && target == AppointmentStatus.IN_PROGRESS)
                    || (current == AppointmentStatus.IN_PROGRESS && target == AppointmentStatus.COMPLETED));
            case PATIENT -> ownPatientAppointment && current == AppointmentStatus.PENDING && target == AppointmentStatus.CANCELLED;
        };
        if (!allowed) {
            if ((role == RoleName.DOCTOR && !assignedDoctor) || (role == RoleName.PATIENT && !ownPatientAppointment))
                throw new AccessDeniedException("You are not allowed to change this appointment");
            throw new InvalidAppointmentStatusException("Invalid status transition for this role");
        }
        appointment.addStatusHistory(new AppointmentStatusHistory(appointment, current, target, actor));
        appointment.setStatus(target);
        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment requestForPatient(PatientAppointmentRequestDTO request, String username) {
        Patient patient = patientRepository.findByAccountUsernameAndArchivedFalse(username)
                .orElseThrow(() -> new PatientNotFoundException("Patient profile is not linked to this account"));
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
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
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
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

    private void requireAvailable(Long doctorId, java.time.LocalDate date, java.time.LocalTime time) {
        if (appointmentRepository.existsByDoctorIdAndDateAndTimeAndStatusNot(doctorId, date, time, AppointmentStatus.CANCELLED))
            throw new AppointmentAlreadyBookedException("Doctor already has an appointment at this time");
    }
}
