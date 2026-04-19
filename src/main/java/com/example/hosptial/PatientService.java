package com.example.hosptial;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PatientService {
    private final PatientRepository  patientRepository ;
public PatientService(PatientRepository patientRepository){
    this.patientRepository = patientRepository;
}
   public void addPatient(Patient patient){
        patientRepository.save(patient);
    }
public Page<Patient> getAllPatient(Pageable pageable){
    return patientRepository.findAll(pageable);
}
public Optional<Patient> getPatientById(Long id){
   return  patientRepository.findById( id);

}
public Patient findOrThrow(Long id){
  
    return patientRepository.findById(id)
    .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
 
  }
  public Patient updatePatient(Long id,PatientReqDTO req){
    
    Patient patient = findOrThrow(id);

    patient.setName(req.getName());
    patient.setAge(req.getAge());
    patient.setGender(req.getGender());
    patient.setContact(req.getContact());
    patient.setAddress(req.getAddress());

    return patientRepository.save(patient);
}
public void deletePatient(Long id){
    findOrThrow(id);
     patientRepository.deleteById(id);
}

  }




