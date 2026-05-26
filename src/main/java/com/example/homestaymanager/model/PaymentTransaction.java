package com.example.homestaymanager.model;

import com.example.homestaymanager.enums.PaymentTransactionStatus;
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
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private String provider;
    private String txnRef;
    private String providerTransactionNo;
    private String responseCode;
    private String message;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentTransactionStatus status;

    private LocalDateTime paidAt;

    @Column(length = 1024)
    private String secureHash;

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
