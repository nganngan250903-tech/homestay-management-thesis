package com.example.homestaymanager.repository;

import com.example.homestaymanager.enums.BookingStatus;
import com.example.homestaymanager.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    @Query("""
            select count(b) from Booking b
            where b.room.id = :roomId
            and b.currentStatus in :statuses
            and b.checkIn < :checkOut
            and b.checkOut > :checkIn
            """)
    long countOverlapping(
            @Param("roomId") int roomId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("statuses") Collection<BookingStatus> statuses);

    @Query("""
            select b from Booking b
            where (:customerId is null or b.customer.id = :customerId)
              and (:roomId is null or b.room.id = :roomId)
              and (:branchId is null or b.room.branch.id = :branchId)
              and (:status is null or b.currentStatus = :status)
            order by b.checkIn desc
            """)
    Page<Booking> findByFilters(
            @Param("customerId") Integer customerId,
            @Param("roomId") Integer roomId,
            @Param("branchId") Integer branchId,
            @Param("status") BookingStatus status,
            Pageable pageable);

    java.util.List<Booking> findByCurrentStatusAndPendingExpiresAtBefore(BookingStatus status, LocalDateTime now);
}
