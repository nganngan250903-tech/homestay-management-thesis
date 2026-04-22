package com.example.homestaymanager.repository;

import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
}


