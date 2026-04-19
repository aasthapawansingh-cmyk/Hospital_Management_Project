package com.example.hosptial.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String token;

     @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private String username;

    public RefreshToken() {}

    public RefreshToken(String token, Instant expiryDate, String username) {
        this.token = token;
        this.expiryDate = expiryDate;
        this.username = username;
    }
     public long getId(){
        return id;
    }
    public void setId(long id){
          this.id=id;
    }
    public String getToken(){
        return token;
    }
    public void setToken(String token){
          this.token=token;
    }
     public String getusername(){
        return username;
    }
    public void setusername(String username){
          this.username=username;
    }
     public Instant getExpiryDate(){
        return expiryDate;
    }
    public void setExpiryDate(Instant expiryDate){
          this.expiryDate= expiryDate;
    }
}
