package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.UpdateEmployee;
import com.example.homestaymanager.dto.response.EmployeeResponse;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.model.Role;
import com.example.homestaymanager.repository.EmployeeRepository;
import com.example.homestaymanager.repository.RoleRepository;
import com.example.homestaymanager.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private  final RoleRepository roleRepository;

    @Override
    public Integer createEmployee(Employee employee) {
        employeeRepository.save(employee);
        return employee.getId();
    }

    @Override
    public EmployeeResponse getEmployeeByID(int id){

        Employee emp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // map employee
        EmployeeResponse res = new EmployeeResponse();
        res.setId(emp.getId());
        res.setName(emp.getName());
        res.setEmail(emp.getEmail());
        res.setPhone(emp.getPhone());
        res.setAddress(emp.getAddress());
        res.setImage(emp.getImage());
        res.setRole(emp.getRole());

        return res;
    }

    @Override
    public void deleteEmployeeById(int id) {
        Employee employee = employeeRepository.findById(id).orElseThrow(()-> new RuntimeException("Employee not found"));
        employeeRepository.delete(employee);
    }

    @Override
    public ArrayList<Employee> getListEmployee() {
        return (ArrayList<Employee>) employeeRepository.findAll();
    }

    @Override
    public EmployeeResponse UpdateEmployeeById(int id, UpdateEmployee newEmp) {
        Employee oldEmp = employeeRepository.findById(id).orElseThrow(()-> new RuntimeException("Không tìm thấy nhân viên"));

        if (newEmp.getName() != null && !newEmp.getName().isBlank()) {
            oldEmp.setName(newEmp.getName());
        }

        if (newEmp.getSalary() != null) {
            oldEmp.setSalary(newEmp.getSalary());
        }

        if (newEmp.getEmail() != null && !newEmp.getEmail().isBlank()) {
            oldEmp.setEmail(newEmp.getEmail());
        }

        if (newEmp.getPhone() != null && !newEmp.getPhone().isBlank()) {
            oldEmp.setPhone(newEmp.getPhone());
        }

        if (newEmp.getAddress() != null && !newEmp.getAddress().isBlank()) {
            oldEmp.setAddress(newEmp.getAddress());
        }

        if (newEmp.getImage() != null && !newEmp.getImage().isBlank()) {
            oldEmp.setImage(newEmp.getImage());
        }

        if (newEmp.getRoleId() != null) {
        Role role = roleRepository.findById(newEmp.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

            oldEmp.setRole(role);
        }

        employeeRepository.save(oldEmp);

        EmployeeResponse res = new EmployeeResponse();
        res.setId(oldEmp.getId());
        res.setName(oldEmp.getName());
        res.setEmail(oldEmp.getEmail());
        res.setPhone(oldEmp.getPhone());
        res.setAddress(oldEmp.getAddress());
        res.setImage(oldEmp.getImage());
        res.setRole(oldEmp.getRole());
        return res;
    }
}
