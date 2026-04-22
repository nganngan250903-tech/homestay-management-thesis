package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.model.Customer;

import com.example.homestaymanager.repository.CustomerRepository;

import com.example.homestaymanager.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public Customer getCustomerByID(int id){
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @Override
    public void deleteCustomerById(int id){
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerRepository.delete(customer);
    }
}