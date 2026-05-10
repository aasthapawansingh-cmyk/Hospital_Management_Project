package com.example.hosptial;

import java.util.List;
import java.util.Optional;

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
public Appointment updateStatus(Long id, AppointmentStatus newStatus){
    Appointment appointment = appointmentRepository.findById(id)
        .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

AppointmentStatus currentStatus = appointment.getStatus();

if (currentStatus == AppointmentStatus.PENDING &&
        (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED)) {
    appointment.setStatus(newStatus);
} else if (currentStatus == AppointmentStatus.CONFIRMED &&
        (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED)) {
    appointment.setStatus(newStatus);
} else {
    throw new InvalidAppointmentStatusException("Invalid status transition");
}

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
    appointment.setStatus(request.getStatus());
    appointment.setPatient(patient);
    appointment.setDoctor(doctor);

    appointmentRepository.save(appointment);
}

}
