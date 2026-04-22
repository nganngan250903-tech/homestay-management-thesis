package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.service.CustomerService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/customers")
    public Integer createCustomer(@RequestBody Customer customer){
        return customerService.createCustomer(customer);
    }

    @GetMapping("/customers/{id}")
    public Customer getCustomerById(@PathVariable int id){
        return customerService.getCustomerByID(id);
    }

    @DeleteMapping("/customers/{id}")
    public void deleteCustomerById(@PathVariable int id){
        customerService.deleteCustomerById(id);
    }
}