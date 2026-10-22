package com.example.hosptial;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(org.springframework.dao.InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<String> handleSchemaMismatch(org.springframework.dao.InvalidDataAccessResourceUsageException ex) {
        return new ResponseEntity<>("The database schema is missing a required column or audit table. Apply the complete V5__doctor_lifecycle_audit.sql migration, then retry.", HttpStatus.SERVICE_UNAVAILABLE);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    @ExceptionHandler(AppointmentAlreadyBookedException.class)
public ResponseEntity<String> handleAppointmentAlreadyBooked(AppointmentAlreadyBookedException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
}
@ExceptionHandler(InvalidAppointmentStatusException.class)
public ResponseEntity<String> handleInvalidAppointmentStatus(InvalidAppointmentStatusException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
}
@ExceptionHandler(AppointmentNotFoundException.class)
public ResponseEntity<String> handleInvalidAppointmentStatus(AppointmentNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
}
@ExceptionHandler(PatientNotFoundException.class)
public ResponseEntity<String> handlePatientNotFound(PatientNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
}
@ExceptionHandler(DoctorNotFoundException.class)
public ResponseEntity<String> handleDoctorNotFound(DoctorNotFoundException ex) {
    return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
}


}
