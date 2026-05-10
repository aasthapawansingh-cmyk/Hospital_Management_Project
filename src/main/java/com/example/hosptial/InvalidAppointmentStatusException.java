package com.example.hosptial;

public class InvalidAppointmentStatusException  extends RuntimeException {
    public InvalidAppointmentStatusException(String message) {
        super(message);
    }
}
