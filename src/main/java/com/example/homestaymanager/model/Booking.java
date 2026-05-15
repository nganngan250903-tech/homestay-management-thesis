package com.example.homestaymanager.model;

import com.example.homestaymanager.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne @JoinColumn(name = "customer_id")
    private Customer customer;
    @ManyToOne @JoinColumn(name = "employee_id")
    private Employee employee;
    @ManyToOne @JoinColumn(name = "room_id")
    private Room room;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private int guestCount;
    private LocalDateTime pendingExpiresAt;
    @Enumerated(EnumType.STRING)
    private BookingStatus currentStatus;
    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private boolean hasSentReminder;
}