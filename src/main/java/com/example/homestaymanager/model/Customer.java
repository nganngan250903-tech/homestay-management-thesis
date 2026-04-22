package com.example.homestaymanager.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String email;
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private  String phone;
    private  String address;
    private  String image;
}






