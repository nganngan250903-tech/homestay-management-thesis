package com.example.homestaymanager.repository;

import com.example.homestaymanager.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByEmailIgnoreCase(String email);

    Optional<Customer> findFirstByPhone(String phone);

    List<Customer> findByNameIgnoreCase(String name);

    List<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
            String name,
            String email,
            String phone);
}
