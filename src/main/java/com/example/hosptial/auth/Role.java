package com.example.hosptial.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Role {
    @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;
     @Enumerated(EnumType.STRING)
@Column(nullable = false, unique = true)
private RoleName name;
public Role(){

}
public Role(Long id,RoleName name){
    this.id=id;
    this.name=name;
}
public void setId(Long id){
    this.id=id;
}
public Long getId(){
    return id;
}
public void setName(RoleName name){
    this.name=name;
}
public RoleName getName(){
    return name;
}
}
