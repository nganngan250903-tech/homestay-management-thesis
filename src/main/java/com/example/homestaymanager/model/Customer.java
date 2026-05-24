package com.example.homestaymanager.model;

import com.example.homestaymanager.enums.AuthProvider;
import com.example.homestaymanager.enums.CustomerStatus;
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
    @Column(nullable = false,unique = true)
    private String email;
    @Column(nullable = true)
    private String password;
    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private  String phone;
    private  String address;
    private  String image;
    @Enumerated(EnumType.STRING)
    private CustomerStatus status = CustomerStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider = AuthProvider.LOCAL;

    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (status == null) {
            status = CustomerStatus.ACTIVE;
        }
        if (provider == null) {
            provider = AuthProvider.LOCAL;
        }
    }
}






