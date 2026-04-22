package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.Branch;

import com.example.homestaymanager.service.BranchService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class BranchController {
    private final BranchService branchService;

    @PostMapping("/branches")
    public Integer createBranch(@RequestBody Branch branch){
        return branchService.createBranch(branch);
    }

    @GetMapping("/branches/{branchesId}")
    public Branch getBranchById(@PathVariable int branchId){
        return branchService.getBranchByID(branchId);

    }

    @DeleteMapping("/branches/{branchesId}")
    public  void  deleteEmployeeById(@PathVariable int branchId){
        branchService.deleteBranchById(branchId);
    }

}
