package com.example.homestaymanager.dto.request;

import lombok.Data;

@Data
public class UpdateBranchRequest {
    private String name;
    private String address;
    private String phone;
    private String image;
}
