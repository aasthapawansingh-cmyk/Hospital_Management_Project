package com.example.hosptial;

import java.util.List;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.stereotype.Service;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    
private final PatientRepository patientRepository;
private final DoctorRepository doctorRepository;

public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository,
                          DoctorRepository doctorRepository){
    this.appointmentRepository = appointmentRepository;
    this.patientRepository = patientRepository;
    this.doctorRepository = doctorRepository;
    
}
public void addAppointment(Appointment appointment){
     if (appointment.getStatus() != null && appointment.getStatus() != AppointmentStatus.PENDING)
         throw new InvalidAppointmentStatusException("New appointments must start as PENDING");
     appointment.setStatus(AppointmentStatus.PENDING);
     appointmentRepository.save(appointment);
}
public List<Appointment> getAllAppointments(){
   return appointmentRepository.findAll();
}
public Optional<Appointment> getAppointmentById(Long id){
    return appointmentRepository.findById(id);
}
public void deleteAppointment(Long id){
     appointmentRepository.deleteById(id);
}
@Transactional
public Appointment updateStatus(Long id, AppointmentStatus newStatus, String actor, boolean admin, boolean assignedDoctor){
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));
    if (!admin && !assignedDoctor) throw new AccessDeniedException("Only the assigned doctor or an admin can update status");
    if (newStatus == null) throw new InvalidAppointmentStatusException("Status is required");
AppointmentStatus currentStatus = appointment.getStatus();
if (!((currentStatus == AppointmentStatus.PENDING && newStatus == AppointmentStatus.IN_PROGRESS)
        || (currentStatus == AppointmentStatus.IN_PROGRESS && newStatus == AppointmentStatus.COMPLETED))) {
    throw new InvalidAppointmentStatusException("Invalid status transition");
}
    appointment.addStatusHistory(new AppointmentStatusHistory(appointment, currentStatus, newStatus, actor));
    appointment.setStatus(newStatus);
return appointmentRepository.save(appointment);

}

public void addAppointment(AppointmentRequestDTO request) {
    Patient patient = patientRepository.findById(request.getPatientId())
            .orElseThrow(() -> new PatientNotFoundException("Patient not found"));

    Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new  DoctorNotFoundException("Doctor not found"));
if (appointmentRepository.existsByDoctorIdAndDateAndTime(
        request.getDoctorId(),
        request.getDate(),
        request.getTime())) {
    throw new AppointmentAlreadyBookedException("Doctor already has an appointment at this time");
}

    Appointment appointment = new Appointment();
    appointment.setDate(request.getDate());
    appointment.setTime(request.getTime());
    if (request.getStatus() != null && request.getStatus() != AppointmentStatus.PENDING)
        throw new InvalidAppointmentStatusException("New appointments must start as PENDING");
    appointment.setStatus(AppointmentStatus.PENDING);
    appointment.setPatient(patient);
    appointment.setDoctor(doctor);

    appointmentRepository.save(appointment);
}

}
