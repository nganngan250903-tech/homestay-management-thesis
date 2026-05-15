package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateCustomerRequest;
import com.example.homestaymanager.dto.response.CustomerResponse;
import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.model.Employee;
import java.util.List;

public interface CustomerService {
    Integer createCustomer(Customer customer);
    CustomerResponse getCustomerByID(int id);
    public void deleteCustomerById(int id);
    List<Customer> getListCustomer();
    CustomerResponse updateCustomerById(int id, UpdateCustomerRequest request);
}

