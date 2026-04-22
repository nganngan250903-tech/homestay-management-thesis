package com.example.homestaymanager.service.impl;
import com.example.homestaymanager.model.Amenity;
import com.example.homestaymanager.repository.AmenityRepository;
import com.example.homestaymanager.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    @Override
    public Integer createAmenity(Amenity amenity) {
        if (amenity.getName() == null || amenity.getName().trim().isEmpty()) {
            throw new RuntimeException("Amenity name không được để trống");
        }

        amenityRepository.save(amenity);
        return amenity.getId();
    }

    @Override
    public Amenity getAmenityByID(int id) {
        return amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found"));
    }

    @Override
    public void deleteAmenityById(int id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Amenity not found"));

        amenityRepository.delete(amenity);
    }
}