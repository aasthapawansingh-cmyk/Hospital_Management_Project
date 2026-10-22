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
public class DoctorStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Doctor doctor;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false)
    private String actorUsername;
    @Column(nullable = false, length = 1000)
    private String reason;
    @Column(nullable = false)
    private Instant changedAt;

    protected DoctorStatusHistory() {}

    public DoctorStatusHistory(Doctor doctor, boolean active, String actorUsername, String reason) {
        this.doctor = doctor;
        this.active = active;
        this.actorUsername = actorUsername;
        this.reason = reason;
        this.changedAt = Instant.now();
    }

    public Long getId() { return id; }
    public boolean isActive() { return active; }
    public String getActorUsername() { return actorUsername; }
    public String getReason() { return reason; }
    public Instant getChangedAt() { return changedAt; }
}
