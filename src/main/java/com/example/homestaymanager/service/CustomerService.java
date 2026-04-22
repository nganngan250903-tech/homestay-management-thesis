package com.example.homestaymanager.service;

import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.model.Employee;

public interface CustomerService {
    Integer createCustomer(Customer customer);
    Customer getCustomerByID(int id);
    public void deleteCustomerById(int id);
}

