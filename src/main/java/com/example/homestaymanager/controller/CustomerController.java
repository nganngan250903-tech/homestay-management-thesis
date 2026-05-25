package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateCustomerRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.BookingResponse;
import com.example.homestaymanager.dto.response.CustomerResponse;
import com.example.homestaymanager.enums.CustomerStatus;
import com.example.homestaymanager.exception.UnauthorizedException;
import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.security.SecurityUtil;
import com.example.homestaymanager.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/customers")
    public ApiResponse<List<CustomerResponse>> getListCustomer(@RequestParam(required = false) String keyword) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Only admin can view customers");
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customerService.getListCustomer(keyword));
    }

    @GetMapping("/customers/lookup")
    public ApiResponse<List<CustomerResponse>> lookupCustomers(@RequestParam(required = false) String keyword) {
        if (!SecurityUtil.isEmployee()) {
            throw new UnauthorizedException("Only staff can lookup customers");
        }
        if (keyword == null || keyword.isBlank()) {
            return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, List.of());
        }
        List<CustomerResponse> customers = customerService.getListCustomer(keyword)
                .stream()
                .limit(10)
                .toList();
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customers);
    }

    @PostMapping("/customers")
    public ApiResponse<Integer> createCustomer(@RequestBody Customer customer) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Only admin can create customers");
        }
        int id = customerService.createCustomer(customer);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, id);
    }

    @GetMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable int id) {
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("No permission to view this customer");
            }
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customerService.getCustomerByID(id));
    }

    @PatchMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> updateCustomerById(@PathVariable int id, @RequestBody UpdateCustomerRequest request) {
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("No permission to update this customer");
            }
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, customerService.updateCustomerById(id, request));
    }

    @PatchMapping("/customers/{id}/status")
    public ApiResponse<CustomerResponse> updateCustomerStatus(@PathVariable int id, @RequestBody UpdateCustomerRequest request) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Only admin can update customer status");
        }
        CustomerStatus status = request != null ? request.getStatus() : null;
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, customerService.updateCustomerStatus(id, status));
    }

    @GetMapping("/customers/{id}/bookings")
    public ApiResponse<List<BookingResponse>> getCustomerBookings(@PathVariable int id) {
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("No permission to view this customer booking history");
            }
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customerService.getCustomerBookings(id));
    }

    @DeleteMapping("/customers/{id}")
    public ApiResponse<?> deleteCustomerById(@PathVariable int id) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Only admin can delete customers");
        }
        customerService.deleteCustomerById(id);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
