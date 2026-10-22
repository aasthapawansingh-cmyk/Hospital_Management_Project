package com.example.hosptial;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.hosptial.auth.UserRepository;
import com.example.hosptial.auth.RoleName;

@Service
public class PatientService {
    private final PatientRepository  patientRepository ;
    private UserRepository users;
public PatientService(PatientRepository patientRepository){
    this.patientRepository = patientRepository;
}
@Autowired
public void setUserRepository(UserRepository users) { this.users = users; }
   public void addPatient(Patient patient){
        validatePatientAccount(patient.getAccountUsername());
        patientRepository.save(patient);
    }
public Page<Patient> getAllPatient(Pageable pageable){
    return patientRepository.findByArchivedFalse(pageable);
}
public Optional<Patient> getPatientById(Long id){
   return  patientRepository.findById( id);

}
public Patient findByAccountUsername(String username) {
    return patientRepository.findByAccountUsernameAndArchivedFalse(username)
            .orElseThrow(() -> new PatientNotFoundException("Patient profile is not linked to this account"));
}
public Patient findOrThrow(Long id){
    Patient patient = patientRepository.findById(id)
        .orElseThrow(() -> new PatientNotFoundException("Patient not found"));
    if (patient.isArchived()) throw new PatientNotFoundException("Patient not found");
    return patient;
  }
  public Patient updatePatient(Long id,PatientReqDTO req){
    
    Patient patient = findOrThrow(id);

    patient.setName(req.getName());
    patient.setAge(req.getAge());
    patient.setGender(req.getGender());
    patient.setContact(req.getContact() == null || req.getContact().isBlank() ? null : req.getContact().trim());
    patient.setAddress(req.getAddress());
    if (req.getAccountUsername() != null && !req.getAccountUsername().isBlank()) {
        String requestedUsername = req.getAccountUsername().trim();
        if (patient.getAccountUsername() != null && !patient.getAccountUsername().equals(requestedUsername))
            throw new IllegalArgumentException("A linked patient login cannot be changed here; create or link the account through administration");
        validatePatientAccount(requestedUsername);
        patient.setAccountUsername(requestedUsername);
    }

    return patientRepository.save(patient);
}
private void validatePatientAccount(String username) {
    if (username == null || username.isBlank() || users == null) return;
    var account = users.findByUsername(username.trim()).orElseThrow(() -> new IllegalArgumentException("Linked patient account does not exist"));
    if (account.getRoles().stream().noneMatch(role -> role.getName() == RoleName.PATIENT))
        throw new IllegalArgumentException("Linked account must have the PATIENT role");
}
public void deletePatient(Long id){
    Patient patient = findOrThrow(id);
    patient.setArchived(true);
    patientRepository.save(patient);
}

  }




