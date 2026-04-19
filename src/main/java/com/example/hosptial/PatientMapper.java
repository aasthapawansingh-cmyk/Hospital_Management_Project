package com.example.hosptial;

public class PatientMapper {
    public static Patient toEntity(PatientReqDTO req){
        
        Patient patient =new Patient();
         patient.setName(req.getName());
    patient.setAge(req.getAge());
    patient.setGender(req.getGender());
    patient.setContact(req.getContact());
    patient.setAddress(req.getAddress());
       return patient;
    }
}
