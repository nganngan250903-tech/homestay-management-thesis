package com.example.homestaymanager.service;

import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Role;

public interface RoleService {

Integer createRole(Role role);
Role getRoleByID(int id);
public void deleteRoleById(int id);
}
