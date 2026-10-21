package com.example.hosptial;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import com.example.hosptial.auth.UserRepository;

@Service
public class RecordAccessService {
    private final UserRepository users;
    public RecordAccessService(UserRepository users) { this.users = users; }
    public boolean isAdmin(String username) {
        return users.findByUsername(username).map(u -> u.getRoles().stream().anyMatch(r -> r.getName().name().equals("ADMIN"))).orElse(false);
    }
    public boolean isDoctorAssigned(String username, Doctor doctor) { return doctor != null && username.equals(doctor.getAccountUsername()); }
    public boolean isPatientOwner(String username, Patient patient) { return patient != null && username.equals(patient.getAccountUsername()); }
    public void requirePatientAccess(String username, Patient patient) {
        if (!isAdmin(username) && !isPatientOwner(username, patient)) throw new AccessDeniedException("Not permitted");
    }
    public boolean isAssignedClinician(String username, Appointment a) { return a != null && isDoctorAssigned(username, a.getDoctor()); }
    public boolean isAppointmentPatient(String username, Appointment a) { return a != null && isPatientOwner(username, a.getPatient()); }
    public void requireAppointmentAccess(String username, Appointment a) {
        if (!isAdmin(username) && !isAssignedClinician(username, a) && !isAppointmentPatient(username, a))
            throw new AccessDeniedException("Not permitted");
    }
}
