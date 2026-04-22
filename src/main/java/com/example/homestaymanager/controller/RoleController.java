package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Role;
import com.example.homestaymanager.service.EmployeeService;
import com.example.homestaymanager.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/roles")
    public Integer createRole(@RequestBody Role role){
        return roleService.createRole(role);
    }

    @GetMapping("/roles/{roleId}")
    public Role getRoleById(@PathVariable int roleId){
        return roleService.getRoleByID(roleId);
    }

    @DeleteMapping("/role/{roleId}")
    public void deleteRoleById(@PathVariable int roleId){
        roleService.deleteRoleById(roleId);
    }
}
