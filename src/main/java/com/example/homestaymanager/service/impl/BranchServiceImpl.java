package com.example.homestaymanager.service.impl;

import com.example.homestaymanager.dto.request.UpdateBranchRequest;
import com.example.homestaymanager.model.Branch;

import com.example.homestaymanager.repository.BranchRepository;

import com.example.homestaymanager.service.BranchService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

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

    @Override
    public List<Branch> getListBranch() {
        return branchRepository.findAll();
    }

    @Override
    public Branch updateBranchById(int id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id).orElseThrow(()-> new RuntimeException("Branch not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            branch.setName(request.getName());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            branch.setAddress(request.getAddress());
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            branch.setPhone(request.getPhone());
        }

        if (request.getImage() != null && !request.getImage().isBlank()) {
            branch.setImage(request.getImage());
        }

        return branchRepository.save(branch);
    }
}


