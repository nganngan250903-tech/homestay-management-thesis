package com.example.homestaymanager.service;
import com.example.homestaymanager.dto.request.UpdateAmenityRequest;
import com.example.homestaymanager.model.Amenity;
import java.util.List;

public interface AmenityService {

    Integer createAmenity(Amenity amenity);

    Amenity getAmenityByID(int id);

    void deleteAmenityById(int id);

    List<Amenity> getListAmenity();

    Amenity updateAmenityById(int id, UpdateAmenityRequest request);
}