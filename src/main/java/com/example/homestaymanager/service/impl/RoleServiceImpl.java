package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.UpdateRoleRequest;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Role;
import com.example.homestaymanager.repository.EmployeeRepository;
import com.example.homestaymanager.repository.RoleRepository;
import com.example.homestaymanager.service.EmployeeService;
import com.example.homestaymanager.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Integer createRole(Role role) {
        roleRepository.save(role);
        return role.getId();
    }

    @Override
    public Role getRoleByID(int id) {
        return  roleRepository.findById(id).orElseThrow(()-> new RuntimeException("Role not found"));
    }

    @Override
    public void deleteRoleById(int id) {
        Role role = roleRepository.findById(id).orElseThrow(()-> new RuntimeException("Role not found"));
        roleRepository.delete(role);
    }

    @Override
    public List<Role> getListRole() {
        return roleRepository.findAll();
    }

    @Override
    public Role updateRoleById(int id, UpdateRoleRequest request) {
        Role role = roleRepository.findById(id).orElseThrow(()-> new RuntimeException("Role not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            role.setName(request.getName());
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            role.setDescription(request.getDescription());
        }

        return roleRepository.save(role);
    }
}
