package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateCustomerRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.dto.response.CustomerResponse;
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
    public ApiResponse<List<Customer>> getListCustomer() {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ admin có thể xem danh sách khách hàng");
        }
        List<Customer> customers = customerService.getListCustomer();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách khách hàng thành công", customers);
    }

    @PostMapping("/customers")
    public ApiResponse<Integer> createCustomer(@RequestBody Customer customer) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ admin có thể tạo khách hàng");
        }
        int id = customerService.createCustomer(customer);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, id);
    }

    @GetMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable int id) {
        // Khách hàng chỉ được xem dữ liệu của chính mình, admin có thể xem tất cả
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("Không có quyền xem thông tin khách hàng này");
            }
        }
        CustomerResponse customer = customerService.getCustomerByID(id);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customer);
    }

    @PatchMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> updateCustomerById(@PathVariable int id, @RequestBody UpdateCustomerRequest request) {
        // Khách hàng chỉ được sửa dữ liệu của chính mình, admin có thể sửa tất cả
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("Không có quyền sửa thông tin khách hàng này");
            }
        }
        CustomerResponse customer = customerService.updateCustomerById(id, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, customer);
    }

    @DeleteMapping("/customers/{id}")
    public ApiResponse<?> deleteCustomerById(@PathVariable int id) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ admin có thể xóa khách hàng");
        }
        customerService.deleteCustomerById(id);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
