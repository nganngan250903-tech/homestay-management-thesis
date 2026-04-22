package com.example.homestaymanager.repository;

import com.example.homestaymanager.model.RoomPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomPhotoRepository extends JpaRepository<RoomPhoto, Integer> {
}