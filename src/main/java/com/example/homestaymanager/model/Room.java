package com.example.homestaymanager.model;

import com.example.homestaymanager.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne @JoinColumn(name = "branch_id")
    private Branch branch;
    @ManyToOne @JoinColumn(name = "roomType_id")
    private RoomType roomType;
    private String name;
    private int number;
    private float area;
    private String thumbnail;
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private RoomStatus status = RoomStatus.AVAILABLE;
}
