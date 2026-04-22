package com.example.homestaymanager.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "roomphotos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private String Photo;

}