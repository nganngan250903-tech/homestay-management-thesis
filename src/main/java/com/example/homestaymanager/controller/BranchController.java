package com.example.homestaymanager.controller;

import com.example.homestaymanager.constant.ApiMessage;
import com.example.homestaymanager.constant.ApiStatus;
import com.example.homestaymanager.dto.request.UpdateBranchRequest;
import com.example.homestaymanager.dto.response.ApiResponse;
import com.example.homestaymanager.model.Branch;

import com.example.homestaymanager.service.BranchService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @GetMapping("/branches")
    public ApiResponse<List<Branch>> getListBranch() {
        List<Branch> branches = branchService.getListBranch();
        return ApiResponse.of(ApiStatus.OK, "Lấy ra danh sách chi nhánh thành công", branches);
    }

    @PostMapping("/branches")
    public ApiResponse<Integer> createBranch(@RequestBody Branch branch) {
        int i = branchService.createBranch(branch);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.CREATED, branch.getId());
    }

    @GetMapping("/branches/{branchId}")
    public ApiResponse<Branch> getBranchById(@PathVariable int branchId) {
        Branch branch = branchService.getBranchByID(branchId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.SUCCESS, branch);
    }

    @PatchMapping("/branches/{branchId}")
    public ApiResponse<Branch> updateBranchById(@PathVariable int branchId, @RequestBody UpdateBranchRequest request) {
        Branch branch = branchService.updateBranchById(branchId, request);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.UPDATED, branch);
    }

    @DeleteMapping("/branches/{branchId}")
    public ApiResponse<?> deleteBranchById(@PathVariable int branchId) {
        branchService.deleteBranchById(branchId);
        return ApiResponse.of(ApiStatus.OK, ApiMessage.DELETED, null);
    }
}
