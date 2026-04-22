package com.example.homestaymanager.controller;

import com.example.homestaymanager.model.Amenity;
import com.example.homestaymanager.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @PostMapping("/amenities")
    public Integer createAmenity(@RequestBody Amenity amenity) {
        return amenityService.createAmenity(amenity);
    }

    @GetMapping("/amenities/{amenityId}")
    public Amenity getAmenityById(@PathVariable int amenityId) {
        return amenityService.getAmenityByID(amenityId);
    }

    @DeleteMapping("/amenities/{amenityId}")
    public void deleteAmenityById(@PathVariable int amenityId) {
        amenityService.deleteAmenityById(amenityId);
    }
}