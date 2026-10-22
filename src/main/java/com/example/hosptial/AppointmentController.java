package com.example.hosptial;

import java.util.List;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.hosptial.auth.RoleName;

@RestController
@RequestMapping("api/v1/appointment")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final AppointmentRepository appointmentRepository;
    private final RecordAccessService access;

    public AppointmentController(AppointmentService appointmentService, AppointmentRepository appointmentRepository, RecordAccessService access) {
        this.appointmentService = appointmentService;
        this.appointmentRepository = appointmentRepository;
        this.access = access;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Appointment addAppointment(@Valid @RequestBody AppointmentRequestDTO appointment) {
        return appointmentService.addAppointment(appointment);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @org.springframework.web.bind.annotation.PatchMapping("{id}/schedule")
    public Appointment reschedule(@PathVariable Long id, @Valid @RequestBody AppointmentRescheduleRequest request,
                                  @AuthenticationPrincipal UserDetails user) {
        return appointmentService.reschedule(id, request, user.getUsername());
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping("/request")
    public Appointment requestAppointment(@Valid @RequestBody PatientAppointmentRequestDTO request,
                                          @AuthenticationPrincipal UserDetails user) {
        return appointmentService.requestForPatient(request, user.getUsername());
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @GetMapping
    public List<Appointment> getAllAppointments(@AuthenticationPrincipal UserDetails user) {
        if (access.isAdmin(user.getUsername())) return appointmentService.getAllAppointments();
        if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")))
            return appointmentRepository.findByDoctorAccountUsername(user.getUsername());
        return appointmentRepository.findByPatientAccountUsername(user.getUsername());
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @GetMapping(path = "{id}")
    public Appointment getAppointmentById(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        Appointment appointment = appointmentService.getAppointmentById(id).orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
        access.requireAppointmentAccess(user.getUsername(), appointment);
        return appointment;
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @org.springframework.web.bind.annotation.PatchMapping("{id}/status")
    public Appointment updateStatus(@PathVariable Long id, @Valid @org.springframework.web.bind.annotation.RequestBody AppointmentStatusRequest request,
                                    @AuthenticationPrincipal UserDetails user) {
        Appointment appointment = appointmentService.getAppointmentById(id).orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
        boolean admin = access.isAdmin(user.getUsername());
        boolean assignedDoctor = access.isAssignedClinician(user.getUsername(), appointment);
        boolean ownPatientAppointment = access.isAppointmentPatient(user.getUsername(), appointment);
        access.requireAppointmentAccess(user.getUsername(), appointment);
        RoleName role = admin ? RoleName.ADMIN : user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR")) ? RoleName.DOCTOR : RoleName.PATIENT;
        return appointmentService.updateStatus(id, request.getStatus(), user.getUsername(), role, assignedDoctor, ownPatientAppointment, request.getReason());
    }

}
