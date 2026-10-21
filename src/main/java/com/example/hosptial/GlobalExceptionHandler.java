package com.example.hosptial;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(org.springframework.security.authentication.BadCredentialsException ex) {
        return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
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
