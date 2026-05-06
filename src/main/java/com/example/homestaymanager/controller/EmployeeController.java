package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateEmployee;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.EmployeeResponse;
import com.example.homestaymanager.model.Employee;
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
        ArrayList<Employee> employees = employeeService.getListEmployee();
        return ApiResponse.of(ApiStatus.OK,"Lấy ra danh sách nhân viên thành công", employees);
    }
    @PostMapping("/employees")
    public ApiResponse<?> createEmployee(@RequestBody Employee employee){
        int i = employeeService.createEmployee(employee);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED,null);
    }

    @GetMapping("/employees/{employeeId}")
    public ApiResponse<EmployeeResponse> getEmployeeById(@PathVariable int employeeId){
        EmployeeResponse employee = employeeService.getEmployeeByID(employeeId);

        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS,employee);
    }

    @PatchMapping("/employees/{employeeId}")
    public ApiResponse<EmployeeResponse> updateEmployeeByID(@PathVariable int employeeId,@RequestBody UpdateEmployee dtoEmp){
        EmployeeResponse emp =  employeeService.UpdateEmployeeById(employeeId,dtoEmp);
        return ApiResponse.of(ApiStatus.OK,ApiMessage.UPDATED,emp);
    }

    @DeleteMapping("/employees/{employeeId}")
    public ApiResponse<?> deleteEmployeeById(@PathVariable int employeeId){
        employeeService.deleteEmployeeById(employeeId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED,null);
    }


}
