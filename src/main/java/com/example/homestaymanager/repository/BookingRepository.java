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
import java.util.Optional;
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
              and (:customerName is null or lower(b.customer.name) like lower(concat('%', :customerName, '%')))
              and (:roomId is null or b.room.id = :roomId)
              and (:branchId is null or b.room.branch.id = :branchId)
              and (:status is null or b.currentStatus = :status)
              and (:dateFrom is null or b.checkIn >= :dateFrom)
              and (:dateTo is null or b.checkIn < :dateTo)
            order by
              case
                when b.currentStatus in (
                  com.example.homestaymanager.enums.BookingStatus.PENDING,
                  com.example.homestaymanager.enums.BookingStatus.CONFIRMED
                ) then 0
                else 1
              end,
              b.createdAt desc,
              b.id desc
            """)
    Page<Booking> findByFilters(
            @Param("customerId") Integer customerId,
            @Param("customerName") String customerName,
            @Param("roomId") Integer roomId,
            @Param("branchId") Integer branchId,
            @Param("status") BookingStatus status,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    java.util.List<Booking> findByCurrentStatusAndPendingExpiresAtBefore(BookingStatus status, LocalDateTime now);

    java.util.List<Booking> findByCurrentStatusAndCheckInBeforeAndActualCheckInAtIsNull(BookingStatus status, LocalDateTime now);

    java.util.List<Booking> findByCurrentStatusAndCheckOutBeforeAndActualCheckOutAtIsNotNull(BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByRoomIdAndCurrentStatusAndActualCheckInAtIsNotNullAndActualCheckOutAtIsNullOrderByActualCheckInAtDesc(
            int roomId,
            BookingStatus status);

    Optional<Booking> findFirstByRoomIdAndCurrentStatusAndActualCheckInAtIsNullAndActualCheckOutAtIsNullAndCheckInLessThanEqualAndCheckOutAfterOrderByCheckInAsc(
            int roomId,
            BookingStatus status,
            LocalDateTime checkIn,
            LocalDateTime checkOut);

    @Query("""
            select count(b) > 0 from Booking b
            where b.room.id = :roomId
              and b.id <> :excludedBookingId
              and b.currentStatus = com.example.homestaymanager.enums.BookingStatus.CONFIRMED
              and b.actualCheckInAt is not null
              and b.actualCheckOutAt is null
            """)
    boolean existsActiveStayInRoomExcluding(
            @Param("roomId") int roomId,
            @Param("excludedBookingId") int excludedBookingId);
}
