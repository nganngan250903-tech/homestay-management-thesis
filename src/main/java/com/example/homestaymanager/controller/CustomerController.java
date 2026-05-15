package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateCustomerRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.CustomerResponse;
import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.model.Employee;
import com.example.homestaymanager.service.CustomerService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/customers")
    public ApiResponse<List<Customer>> getListCustomer() {
        List<Customer> customers = customerService.getListCustomer();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách khách hàng thành công", customers);
    }

    @PostMapping("/customers")
    public ApiResponse<Integer> createCustomer(@RequestBody Customer customer) {
        int id = customerService.createCustomer(customer);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, id);
    }

    @GetMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable int id) {
        CustomerResponse customer = customerService.getCustomerByID(id);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customer);
    }

    @PatchMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> updateCustomerById(@PathVariable int id, @RequestBody UpdateCustomerRequest request) {
        CustomerResponse customer = customerService.updateCustomerById(id, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, customer);
    }

    @DeleteMapping("/customers/{id}")
    public ApiResponse<?> deleteCustomerById(@PathVariable int id) {
        customerService.deleteCustomerById(id);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
