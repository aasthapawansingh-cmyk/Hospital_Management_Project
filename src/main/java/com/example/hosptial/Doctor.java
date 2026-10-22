package com.example.hosptial;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Doctor {
       @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
    private String name;
    private String specialization;
    private String contact;
    @jakarta.persistence.Column(nullable = false)
    private boolean active = true;
    @jakarta.persistence.Column(unique = true)
    private String accountUsername;
    public  Doctor(Long id,String name,String specialization,String contact){
        this.id=id;
        this.name=name;
        this.specialization=specialization;
        this.contact=contact;

    }
    public Doctor(){

    }
     public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization( String specialization) {
        this.specialization = specialization;
    }

   

    public String getContact() {
        return contact;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getAccountUsername() { return accountUsername; }
    public void setAccountUsername(String accountUsername) { this.accountUsername = accountUsername; }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }


}
