package com.example.homestaymanager.model;

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
    private int Number;
    private float area;
    private String thumbnail;
}