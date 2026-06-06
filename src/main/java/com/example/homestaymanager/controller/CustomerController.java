package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.CreateQuickCustomerRequest;
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
        if (!SecurityUtil.isEmployee()) {
            throw new UnauthorizedException("Chỉ nhân viên hoặc quản trị viên có thể xem danh sách khách hàng");
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customerService.getListCustomer(keyword));
    }

    @GetMapping("/customers/lookup")
    public ApiResponse<List<CustomerResponse>> lookupCustomers(@RequestParam(required = false) String keyword) {
        if (!SecurityUtil.isEmployee()) {
            throw new UnauthorizedException("Chỉ nhân viên hoặc quản trị viên có thể tra cứu khách hàng");
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
            throw new UnauthorizedException("Chỉ quản trị viên có thể tạo khách hàng");
        }
        int id = customerService.createCustomer(customer);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, id);
    }

    @PostMapping("/customers/quick")
    public ApiResponse<CustomerResponse> createQuickCustomer(@RequestBody CreateQuickCustomerRequest request) {
        if (!SecurityUtil.isAdmin() && !SecurityUtil.isEmployee()) {
            throw new UnauthorizedException("Chỉ nhân viên hoặc quản trị viên có thể tạo nhanh khách hàng");
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, customerService.createQuickCustomer(request));
    }

    @GetMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable int id) {
        if (!SecurityUtil.isEmployee()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("Không có quyền xem thông tin khách hàng này");
            }
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customerService.getCustomerByID(id));
    }

    @PatchMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> updateCustomerById(@PathVariable int id, @RequestBody UpdateCustomerRequest request) {
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("Không có quyền cập nhật thông tin khách hàng này");
            }
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, customerService.updateCustomerById(id, request));
    }

    @PatchMapping("/customers/{id}/status")
    public ApiResponse<CustomerResponse> updateCustomerStatus(@PathVariable int id, @RequestBody UpdateCustomerRequest request) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ quản trị viên có thể cập nhật trạng thái khách hàng");
        }
        CustomerStatus status = request != null ? request.getStatus() : null;
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, customerService.updateCustomerStatus(id, status));
    }

    @GetMapping("/customers/{id}/bookings")
    public ApiResponse<List<BookingResponse>> getCustomerBookings(@PathVariable int id) {
        if (!SecurityUtil.isAdmin()) {
            var current = SecurityUtil.getCurrentUser();
            if (current == null || current.getId() != id) {
                throw new UnauthorizedException("Không có quyền xem lịch sử đặt phòng của khách hàng này");
            }
        }
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, customerService.getCustomerBookings(id));
    }

    @DeleteMapping("/customers/{id}")
    public ApiResponse<?> deleteCustomerById(@PathVariable int id) {
        if (!SecurityUtil.isAdmin()) {
            throw new UnauthorizedException("Chỉ quản trị viên có thể xóa khách hàng");
        }
        customerService.deleteCustomerById(id);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
