package com.example.hosptial;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
public class AppointmentStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Appointment appointment;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AppointmentStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private AppointmentStatus toStatus;
    @Column(nullable = false)
    private String actorUsername;
    @Column(nullable = false)
    private Instant changedAt;
    @Column(length = 1000)
    private String reason;

    protected AppointmentStatusHistory() {}
    public AppointmentStatusHistory(Appointment appointment, AppointmentStatus fromStatus, AppointmentStatus toStatus, String actorUsername) {
        this.appointment = appointment; this.fromStatus = fromStatus; this.toStatus = toStatus;
        this.actorUsername = actorUsername; this.changedAt = Instant.now();
    }
    public AppointmentStatusHistory(Appointment appointment, AppointmentStatus fromStatus, AppointmentStatus toStatus, String actorUsername, String reason) {
        this(appointment, fromStatus, toStatus, actorUsername);
        this.reason = reason;
    }
    public Long getId() { return id; }
    public AppointmentStatus getFromStatus() { return fromStatus; }
    public AppointmentStatus getToStatus() { return toStatus; }
    public String getActorUsername() { return actorUsername; }
    public Instant getChangedAt() { return changedAt; }
    public String getReason() { return reason; }
}
