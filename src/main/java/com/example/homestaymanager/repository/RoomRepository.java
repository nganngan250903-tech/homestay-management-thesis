package com.example.homestaymanager.repository;

import com.example.homestaymanager.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
}