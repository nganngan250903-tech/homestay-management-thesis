package com.example.homestaymanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "RoomAmenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomAmenity {
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(nullable = false)
    private int quantity;
}