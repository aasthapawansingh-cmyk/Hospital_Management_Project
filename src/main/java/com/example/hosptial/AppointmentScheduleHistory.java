package com.example.hosptial;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;

@Entity
public class AppointmentScheduleHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Appointment appointment;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Doctor fromDoctor;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) private Doctor toDoctor;
    @Column(nullable = false) private LocalDate fromDate;
    @Column(nullable = false) private LocalTime fromTime;
    @Column(nullable = false) private LocalDate toDate;
    @Column(nullable = false) private LocalTime toTime;
    @Column(nullable = false) private String actorUsername;
    @Column(nullable = false, length = 1000) private String reason;
    @Column(nullable = false) private Instant changedAt;
    protected AppointmentScheduleHistory() {}
    public AppointmentScheduleHistory(Appointment appointment, Doctor fromDoctor, Doctor toDoctor, LocalDate fromDate, LocalTime fromTime,
                                      LocalDate toDate, LocalTime toTime, String actorUsername, String reason) {
        this.appointment = appointment; this.fromDoctor = fromDoctor; this.toDoctor = toDoctor;
        this.fromDate = fromDate; this.fromTime = fromTime; this.toDate = toDate; this.toTime = toTime;
        this.actorUsername = actorUsername; this.reason = reason; this.changedAt = Instant.now();
    }
    public Long getId() { return id; }
    public Doctor getFromDoctor() { return fromDoctor; }
    public Doctor getToDoctor() { return toDoctor; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalTime getFromTime() { return fromTime; }
    public LocalDate getToDate() { return toDate; }
    public LocalTime getToTime() { return toTime; }
    public String getActorUsername() { return actorUsername; }
    public String getReason() { return reason; }
    public Instant getChangedAt() { return changedAt; }
}
