package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateCustomerRequest;
import com.example.homestaymanager.dto.request.CreateQuickCustomerRequest;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.dto.response.CustomerResponse;
import com.example.homestaymanager.enums.CustomerStatus;
import com.example.homestaymanager.model.Customer;
import java.util.List;

public interface CustomerService {
    Integer createCustomer(Customer customer);
    CustomerResponse createQuickCustomer(CreateQuickCustomerRequest request);
    CustomerResponse getCustomerByID(int id);
    public void deleteCustomerById(int id);
    List<CustomerResponse> getListCustomer(String keyword);
    CustomerResponse updateCustomerById(int id, UpdateCustomerRequest request);
    CustomerResponse updateCustomerStatus(int id, CustomerStatus status);
    List<BookingResponse> getCustomerBookings(int id);
}

