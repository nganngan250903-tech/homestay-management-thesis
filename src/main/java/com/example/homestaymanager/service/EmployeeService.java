package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateEmployee;
import com.example.homestaymanager.dto.response.EmployeeResponse;
import com.example.homestaymanager.model.Employee;

import java.util.ArrayList;

public interface EmployeeService {
    Integer createEmployee(Employee employee);
     public void deleteEmployeeById(int id);
    ArrayList<Employee> getListEmployee();
    EmployeeResponse  UpdateEmployeeById(int id, UpdateEmployee dtoEmp );
    EmployeeResponse getEmployeeByID(int id);
}
