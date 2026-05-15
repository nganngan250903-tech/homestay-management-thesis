package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateRoleRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Role;
import com.example.homestaymanager.service.EmployeeService;
import com.example.homestaymanager.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.aspectj.bridge.Message;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/roles")
    public ApiResponse<List<Role>> getListRole() {
        List<Role> roles = roleService.getListRole();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách vai trò thành công", roles);
    }

    @PostMapping("/roles")
    public ApiResponse<Integer> createRole(@RequestBody Role role) {
        int i = roleService.createRole(role);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, role.getId());
    }

    @GetMapping("/roles/{roleId}")
    public ApiResponse<Role> getRoleById(@PathVariable int roleId) {
        Role role = roleService.getRoleByID(roleId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, role);
    }

    @PatchMapping("/roles/{roleId}")
    public ApiResponse<Role> updateRoleById(@PathVariable int roleId, @RequestBody UpdateRoleRequest request) {
        Role role = roleService.updateRoleById(roleId, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, role);
    }

    @DeleteMapping("/roles/{roleId}")
    public ApiResponse<?> deleteRoleById(@PathVariable int roleId) {
        roleService.deleteRoleById(roleId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
