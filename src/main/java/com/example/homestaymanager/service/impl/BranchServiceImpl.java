package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.model.Branch;

import com.example.homestaymanager.repository.BranchRepository;

import com.example.homestaymanager.service.BranchService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {
    private  final BranchRepository branchRepository;
    @Override
    public Integer createBranch(Branch branch) {
        branchRepository.save(branch);
        return branch.getId();
    }

    @Override
    public Branch getBranchByID(int id) {
        return branchRepository.findById(id).orElseThrow(()-> new RuntimeException("Branch not found"));
    }

    @Override
    public void deleteBranchById(int id) {
        Branch branch = branchRepository.findById(id).orElseThrow(()-> new RuntimeException("Branch not found"));
        branchRepository.delete(branch);
    }
}


