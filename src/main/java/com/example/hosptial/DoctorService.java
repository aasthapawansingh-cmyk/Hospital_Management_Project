package com.example.hosptial;

import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.hosptial.auth.UserRepository;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointments;
    private final DoctorStatusHistoryRepository statusHistory;
    private final UserRepository users;
    public DoctorService(DoctorRepository doctorRepository, AppointmentRepository appointments, DoctorStatusHistoryRepository statusHistory, UserRepository users){
        this.doctorRepository=doctorRepository;
        this.appointments=appointments; this.statusHistory=statusHistory; this.users=users;
    }
    public void addDoctor(Doctor doctor){
      if (doctor.getAccountUsername() != null && !doctor.getAccountUsername().isBlank()) {
          var account = users.findByUsername(doctor.getAccountUsername().trim())
                  .orElseThrow(() -> new IllegalArgumentException("Linked doctor account does not exist"));
          if (account.getRoles().stream().noneMatch(role -> role.getName() == com.example.hosptial.auth.RoleName.DOCTOR))
              throw new IllegalArgumentException("Linked account must have the DOCTOR role");
          doctor.setAccountUsername(account.getUsername());
      }
      doctorRepository.save(doctor);
    }
    public List<Doctor> getAllDoctors(){
       return doctorRepository.findAll();
    }
    public Optional<Doctor> getDoctorById(Long id){
        return doctorRepository.findById(id);
    }
    @Transactional(readOnly = true)
    public List<Appointment> getOffboardingAppointments(Long id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        return upcomingAppointments(doctor);
    }

    @Transactional
    public Doctor deactivate(Long id, DoctorDeactivationRequest request, String actor) {
        Doctor doctor = doctorRepository.findByIdForUpdate(id).orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        if (!doctor.isActive()) return doctor;
        String reason = request.getReason().trim();
        List<Appointment> upcoming = upcomingAppointments(doctor);
        List<DoctorAppointmentResolution> resolutions = request.getResolutions() == null ? List.of() : request.getResolutions();
        Map<Long, DoctorAppointmentResolution> byId = resolutions.stream().collect(Collectors.toMap(DoctorAppointmentResolution::getAppointmentId, Function.identity(), (a,b) -> { throw new IllegalArgumentException("Duplicate appointment resolution"); }));
        if (byId.size() != upcoming.size() || upcoming.stream().anyMatch(a -> !byId.containsKey(a.getId())))
            throw new IllegalArgumentException("Resolve every future pending or confirmed appointment before deactivation");
        for (Appointment appointment : upcoming) {
            DoctorAppointmentResolution resolution = byId.get(appointment.getId());
            if (resolution.getAction() == DoctorAppointmentResolution.Action.CANCEL) {
                AppointmentStatus previous = appointment.getStatus();
                appointment.setStatus(AppointmentStatus.CANCELLED);
                appointment.addStatusHistory(new AppointmentStatusHistory(appointment, previous, AppointmentStatus.CANCELLED, actor, reason));
                appointments.save(appointment);
            } else if (resolution.getAction() == DoctorAppointmentResolution.Action.REASSIGN) {
                if (resolution.getReplacementDoctorId() == null) throw new IllegalArgumentException("Select an active replacement doctor");
                Doctor replacement = doctorRepository.findByIdForUpdate(resolution.getReplacementDoctorId()).orElseThrow(() -> new DoctorNotFoundException("Replacement doctor not found"));
                if (!replacement.isActive() || replacement.getId().equals(id)) throw new IllegalArgumentException("Replacement doctor must be a different active doctor");
                if (appointments.existsByDoctorIdAndDateAndTimeAndStatusNot(replacement.getId(), appointment.getDate(), appointment.getTime(), AppointmentStatus.CANCELLED))
                    throw new AppointmentAlreadyBookedException("Replacement doctor is already booked for " + appointment.getDate() + " " + appointment.getTime());
                appointment.addAssignmentHistory(new AppointmentAssignmentHistory(appointment, doctor, replacement, actor, reason));
                appointment.setDoctor(replacement);
                appointments.save(appointment);
            } else throw new IllegalArgumentException("Choose cancel or reassign for each appointment");
        }
        doctor.setActive(false);
        if (doctor.getAccountUsername() != null) users.findByUsername(doctor.getAccountUsername()).ifPresent(user -> { user.setEnabled(false); users.save(user); });
        doctorRepository.save(doctor);
        statusHistory.save(new DoctorStatusHistory(doctor, false, actor, reason));
        return doctor;
    }

    private List<Appointment> upcomingAppointments(Doctor doctor) {
        return appointments.findUpcomingForDoctor(doctor.getId(), List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED), LocalDate.now(), LocalTime.now());
    }

    @Transactional
    public Doctor activate(Long id, String reason, String actor) {
        Doctor doctor = doctorRepository.findByIdForUpdate(id).orElseThrow(() -> new DoctorNotFoundException("Doctor not found"));
        if (doctor.isActive()) return doctor;
        doctor.setActive(true);
        if (doctor.getAccountUsername() != null) users.findByUsername(doctor.getAccountUsername()).ifPresent(user -> { user.setEnabled(true); users.save(user); });
        doctorRepository.save(doctor);
        statusHistory.save(new DoctorStatusHistory(doctor, true, actor, reason.trim()));
        return doctor;
    }
}
