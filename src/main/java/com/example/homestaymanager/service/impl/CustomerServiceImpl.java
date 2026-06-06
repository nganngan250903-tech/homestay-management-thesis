package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.CreateQuickCustomerRequest;
import com.example.homestaymanager.dto.request.UpdateCustomerRequest;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.dto.response.CustomerResponse;
import com.example.homestaymanager.enums.AuthProvider;
import com.example.homestaymanager.enums.CustomerStatus;
import com.example.homestaymanager.model.Customer;

import com.example.homestaymanager.repository.BookingRepository;
import com.example.homestaymanager.repository.CustomerRepository;
import com.example.homestaymanager.repository.EmployeeRepository;

import com.example.homestaymanager.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public Integer createCustomer(Customer customer){
        if (customer.getProvider() == null) {
            customer.setProvider(AuthProvider.LOCAL);
        }
        customerRepository.save(customer);
        return customer.getId();
    }

    @Override
    @Transactional
    public CustomerResponse createQuickCustomer(CreateQuickCustomerRequest request) {
        if (request == null) {
            throw new RuntimeException("Thông tin khách hàng là bắt buộc");
        }
        String name = normalize(request.getName());
        String phone = normalize(request.getPhone());
        String email = normalizeEmail(request.getEmail());
        if (name == null) {
            throw new RuntimeException("Tên khách hàng là bắt buộc");
        }
        if (phone == null) {
            throw new RuntimeException("Số điện thoại khách hàng là bắt buộc");
        }

        var existingByPhone = customerRepository.findFirstByPhone(phone);
        if (existingByPhone.isPresent()) {
            return toResponse(existingByPhone.get());
        }

        if (email != null) {
            var existingByEmail = customerRepository.findByEmailIgnoreCase(email);
            if (existingByEmail.isPresent()) {
                return toResponse(existingByEmail.get());
            }
            if (employeeRepository.findByEmail(email).isPresent()) {
                throw new RuntimeException("Email đã được sử dụng bởi tài khoản nhân viên");
            }
        } else {
            email = buildWalkInEmail(phone);
        }

        Customer customer = new Customer();
        customer.setName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(normalize(request.getAddress()));
        customer.setPassword(null);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setProvider(AuthProvider.LOCAL);
        customerRepository.save(customer);
        return toResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByID(int id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return toResponse(customer);
    }

    @Override
    public void deleteCustomerById(int id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(customer);
    }

    @Override
    public List<CustomerResponse> getListCustomer(String keyword) {
        List<Customer> customers;
        if (keyword == null || keyword.isBlank()) {
            customers = customerRepository.findAll();
        } else {
            String normalized = keyword.trim();
            customers = customerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrPhoneContainingIgnoreCase(
                    normalized,
                    normalized,
                    normalized);
        }
        return customers.stream().map(CustomerServiceImpl::toResponse).collect(Collectors.toList());
    }

    @Override
    public CustomerResponse updateCustomerById(int id, UpdateCustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            customer.setEmail(request.getEmail());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            customer.setPassword(request.getPassword());
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            customer.setName(request.getName());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customer.setPhone(request.getPhone());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            customer.setAddress(request.getAddress());
        }

        if (request.getImage() != null && !request.getImage().isBlank()) {
            customer.setImage(request.getImage());
        }

        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        customerRepository.save(customer);

        return toResponse(customer);
    }

    @Override
    public CustomerResponse updateCustomerStatus(int id, CustomerStatus status) {
        if (status == null) {
            throw new RuntimeException("Status is required");
        }
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setStatus(status);
        customerRepository.save(customer);
        return toResponse(customer);
    }

    @Override
    public List<BookingResponse> getCustomerBookings(int id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found");
        }
        return bookingRepository.findByFilters(id, null, null, null, null, null, null, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .map(BookingServiceImpl::toResponse)
                .collect(Collectors.toList());
    }

    private static CustomerResponse toResponse(Customer customer) {
        CustomerResponse res = new CustomerResponse();
        res.setId(customer.getId());
        res.setEmail(customer.getEmail());
        res.setName(customer.getName());
        res.setPhone(customer.getPhone());
        res.setAddress(customer.getAddress());
        res.setImage(customer.getImage());
        res.setStatus(customer.getStatus() != null ? customer.getStatus() : CustomerStatus.ACTIVE);
        res.setProvider(customer.getProvider() != null ? customer.getProvider() : AuthProvider.LOCAL);
        return res;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String normalizeEmail(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private static String buildWalkInEmail(String phone) {
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            digits = String.valueOf(System.currentTimeMillis());
        }
        return "walkin-" + digits + "@limdim.local";
    }
}
