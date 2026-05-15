package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.UpdateCustomerRequest;
import com.example.homestaymanager.dto.response.CustomerResponse;
import com.example.homestaymanager.model.Customer;

import com.example.homestaymanager.repository.CustomerRepository;

import com.example.homestaymanager.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Integer createCustomer(Customer customer){
        customerRepository.save(customer);
        return customer.getId();
    }

    @Override
    public CustomerResponse getCustomerByID(int id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        CustomerResponse res = new CustomerResponse();
        res.setId(customer.getId());
        res.setEmail(customer.getEmail());
        res.setName(customer.getName());
        res.setPhone(customer.getPhone());
        res.setAddress(customer.getAddress());
        res.setImage(customer.getImage());

        return res;
    }

    @Override
    public void deleteCustomerById(int id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(customer);
    }

    @Override
    public List<Customer> getListCustomer() {
        return customerRepository.findAll();
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

        customerRepository.save(customer);

        CustomerResponse res = new CustomerResponse();
        res.setId(customer.getId());
        res.setEmail(customer.getEmail());
        res.setName(customer.getName());
        res.setPhone(customer.getPhone());
        res.setAddress(customer.getAddress());
        res.setImage(customer.getImage());

        return res;
    }
}