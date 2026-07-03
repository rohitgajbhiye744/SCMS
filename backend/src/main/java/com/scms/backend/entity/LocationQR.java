package com.scms.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "qr_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationQR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String qrCodeUuid;

    @Column(nullable = false)
    private String building;

    @Column(nullable = false)
    private String roomNumber;

    private String equipment;
}
