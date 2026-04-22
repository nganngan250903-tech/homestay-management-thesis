package com.example.homestaymanager.service;

import com.example.homestaymanager.model.Branch;


public interface BranchService {
    Integer createBranch(Branch branch);
    Branch getBranchByID(int id);
    public void deleteBranchById(int id);
}
