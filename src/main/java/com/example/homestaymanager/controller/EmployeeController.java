package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateEmployee;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.EmployeeResponse;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.security.SecurityUtil;
import com.example.homestaymanager.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public ApiResponse<ArrayList<Employee>> getListEmployee (){
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ admin có thể xem danh sách nhân viên");
        }
        ArrayList<Employee> employees = employeeService.getListEmployee();
        return ApiResponse.of(ApiStatus.OK,"Lấy ra danh sách nhân viên thành công", employees);
    }
    @PostMapping("/employees")
    public ApiResponse<?> createEmployee(@RequestBody Employee employee){
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ admin có thể tạo nhân viên");
        }
        int i = employeeService.createEmployee(employee);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED,null);
    }

    @GetMapping("/employees/{employeeId}")
    public ApiResponse<EmployeeResponse> getEmployeeById(@PathVariable int employeeId){
        // Nhân viên có thể xem thông tin của chính mình, admin có thể xem tất cả
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != employeeId) {
                throw new UnauthorizedException("Không có quyền xem thông tin nhân viên này");
            }
        }
        EmployeeResponse employee = employeeService.getEmployeeByID(employeeId);

        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS,employee);
    }

    @PatchMapping("/employees/{employeeId}")
    public ApiResponse<EmployeeResponse> updateEmployeeByID(@PathVariable int employeeId,@RequestBody UpdateEmployee dtoEmp){
        // Nhân viên có thể sửa thông tin của chính mình, admin có thể sửa tất cả
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != employeeId) {
                throw new UnauthorizedException("Không có quyền sửa thông tin nhân viên này");
            }
        }
        EmployeeResponse emp =  employeeService.UpdateEmployeeById(employeeId,dtoEmp);
        return ApiResponse.of(ApiStatus.OK,ApiMessage.UPDATED,emp);
    }

    @DeleteMapping("/employees/{employeeId}")
    public ApiResponse<?> deleteEmployeeById(@PathVariable int employeeId){
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ admin có thể xóa nhân viên");
        }
        employeeService.deleteEmployeeById(employeeId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED,null);
    }


}
