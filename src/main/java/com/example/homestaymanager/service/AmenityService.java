package com.example.homestaymanager.service;
import com.example.homestaymanager.model.Amenity;

public interface AmenityService {

    Integer createAmenity(Amenity amenity);

    Amenity getAmenityByID(int id);

    void deleteAmenityById(int id);
}