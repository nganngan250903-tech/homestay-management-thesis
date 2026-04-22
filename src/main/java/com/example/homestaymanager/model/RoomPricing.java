package com.example.homestaymanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomPricing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne @JoinColumn(name = "roomType_id")
    private RoomType roomType;
    @Column(nullable = false)
    private String baseDuration;
    private BigDecimal basePrice;
    private BigDecimal weekendPrice;
    private BigDecimal holidayPrice;
    @Column( nullable = false)
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String policy;
    private Boolean status;
}