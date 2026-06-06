package com.example.homestaymanager.repository;

import com.example.homestaymanager.enums.RoomStatus;
import com.example.homestaymanager.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {
    long countByRoomTypeId(int roomTypeId);

    @Query("select r from Room r where r.active = true or r.active is null")
    List<Room> findVisibleRooms();

    @Query("""
            select r from Room r
            where r.status = :status
              and r.cleaningStartedAt < :before
              and (r.active = true or r.active is null)
            """)
    List<Room> findVisibleRoomsByStatusAndCleaningStartedAtBefore(
            @Param("status") RoomStatus status,
            @Param("before") LocalDateTime before);
}
