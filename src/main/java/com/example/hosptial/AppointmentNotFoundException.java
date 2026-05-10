package com.example.hosptial;

public class AppointmentNotFoundException extends RuntimeException {
     public AppointmentNotFoundException(String message) {
        super(message);
    }
}
