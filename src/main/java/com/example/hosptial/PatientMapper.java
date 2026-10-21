package com.example.hosptial;

public class PatientMapper {
    public static Patient toEntity(PatientReqDTO req){
        
        Patient patient =new Patient();
         patient.setName(req.getName());
    patient.setAge(req.getAge());
    patient.setGender(req.getGender());
        patient.setContact(normalize(req.getContact()));
    patient.setAddress(req.getAddress());
        patient.setAccountUsername(normalize(req.getAccountUsername()));
       return patient;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
