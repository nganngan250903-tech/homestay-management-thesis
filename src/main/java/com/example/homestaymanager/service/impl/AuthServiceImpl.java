package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.LoginRequest;
import com.example.homestaymanager.dto.request.RegisterRequest;
import com.example.homestaymanager.dto.response.AuthUserResponse;
import com.example.homestaymanager.dto.response.LoginResponse;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.repository.CustomerRepository;
import com.example.homestaymanager.repository.EmployeeRepository;
import com.example.homestaymanager.service.AuthService;
import com.example.homestaymanager.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String CUSTOMER = "CUSTOMER";
    private static final String EMPLOYEE = "EMPLOYEE";
    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    @Override
    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        Employee employee = employeeRepository.findByEmail(request.getEmail()).orElse(null);
        if (employee != null && passwordMatches(request.getPassword(), employee.getPassword())) {
            return employeeLoginResponse(employee);
        }

        Customer customer = customerRepository.findByEmail(request.getEmail()).orElse(null);
        if (customer != null && passwordMatches(request.getPassword(), customer.getPassword())) {
            return customerLoginResponse(customer);
        }

        throw new UnauthorizedException("Email hoăc mật khẩu của bạn chưa đúng!");
    }

    @Override
    public LoginResponse registerCustomer(RegisterRequest request) {
        if (request == null) {
            throw new RuntimeException("Request is required");
        }
        if (isBlank(request.getEmail()) || isBlank(request.getPassword()) || isBlank(request.getName()) || isBlank(request.getPhone())) {
            throw new RuntimeException("Name, email, password and phone are required");
        }
        if (customerRepository.findByEmail(request.getEmail()).isPresent()
                || employeeRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Customer customer = new Customer();
        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPassword(request.getPassword());
        customer.setPhone(request.getPhone());
        customerRepository.save(customer);

        return customerLoginResponse(customer);
    }

    private LoginResponse employeeLoginResponse(Employee employee) {
        String role = employee.getRole() != null ? employee.getRole().getName() : EMPLOYEE;
        AuthUserResponse user = AuthUserResponse.builder()
                .id(employee.getId())
                .email(employee.getEmail())
                .name(employee.getName())
                .phone(employee.getPhone())
                .role(role)
                .build();

        return LoginResponse.builder()
                .token(jwtService.generateToken(employee.getId(), employee.getEmail(), EMPLOYEE, role))
                .userType(EMPLOYEE)
                .role(role)
                .user(user)
                .build();
    }

    private LoginResponse customerLoginResponse(Customer customer) {
        AuthUserResponse user = AuthUserResponse.builder()
                .id(customer.getId())
                .email(customer.getEmail())
                .name(customer.getName())
                .phone(customer.getPhone())
                .role(CUSTOMER_ROLE)
                .build();

        return LoginResponse.builder()
                .token(jwtService.generateToken(customer.getId(), customer.getEmail(), CUSTOMER, CUSTOMER_ROLE))
                .userType(CUSTOMER)
                .role(CUSTOMER_ROLE)
                .user(user)
                .build();
    }

    private static void validateLoginRequest(LoginRequest request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            throw new UnauthorizedException("Email or password is incorrect");
        }
    }

    private static boolean passwordMatches(String rawPassword, String storedPassword) {
        return storedPassword != null && storedPassword.equals(rawPassword);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
