package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.ApiResponse;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/employees")
    public ApiResponse<?> createEmployee(@RequestBody Employee employee){
        employeeService.createEmployee(employee);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED,null);
    }

    @GetMapping("/employees/{employeeId}")
    public ApiResponse<Employee> getEmployeeById(@PathVariable int employeeId){
        Employee employee = employeeService.getEmployeeByID(employeeId);

        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS,employee);
    }

    @DeleteMapping("/employees/{employeeId}")
    public ApiResponse<?> deleteEmployeeById(@PathVariable int employeeId){
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED,null);
    }
}
