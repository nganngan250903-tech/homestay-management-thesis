package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateRoleRequest;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Role;
import java.util.List;

public interface RoleService {
    Integer createRole(Role role);
    Role getRoleByID(int id);
    public void deleteRoleById(int id);
    List<Role> getListRole();
    Role updateRoleById(int id, UpdateRoleRequest request);
}
