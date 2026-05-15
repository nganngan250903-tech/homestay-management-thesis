package com.example.homestaymanager.service;

import com.example.homestaymanager.dto.request.UpdateBranchRequest;
import com.example.homestaymanager.model.Branch;
import java.util.List;

public interface BranchService {
    Integer createBranch(Branch branch);
    Branch getBranchByID(int id);
    public void deleteBranchById(int id);
    List<Branch> getListBranch();
    Branch updateBranchById(int id, UpdateBranchRequest request);
}
