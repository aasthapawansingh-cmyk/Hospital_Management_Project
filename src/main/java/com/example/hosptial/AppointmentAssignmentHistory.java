package com.example.hosptial;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class AppointmentAssignmentHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Appointment appointment;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Doctor fromDoctor;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Doctor toDoctor;
    @Column(nullable = false)
    private String actorUsername;
    @Column(nullable = false, length = 1000)
    private String reason;
    @Column(nullable = false)
    private Instant changedAt;

    protected AppointmentAssignmentHistory() {}

    public AppointmentAssignmentHistory(Appointment appointment, Doctor fromDoctor, Doctor toDoctor, String actorUsername, String reason) {
        this.appointment = appointment;
        this.fromDoctor = fromDoctor;
        this.toDoctor = toDoctor;
        this.actorUsername = actorUsername;
        this.reason = reason;
        this.changedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Doctor getFromDoctor() { return fromDoctor; }
    public Doctor getToDoctor() { return toDoctor; }
    public String getActorUsername() { return actorUsername; }
    public String getReason() { return reason; }
    public Instant getChangedAt() { return changedAt; }
}
