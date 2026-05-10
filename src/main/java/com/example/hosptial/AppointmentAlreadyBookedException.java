package com.example.hosptial;

public class AppointmentAlreadyBookedException extends RuntimeException {
    public AppointmentAlreadyBookedException(String message) {
        super(message);
    }
}
