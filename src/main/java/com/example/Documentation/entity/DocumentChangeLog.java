package com.example.Documentation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class DocumentChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;
    private String username;
    private LocalDateTime timestamp;
    private String operation; // CREATE, UPDATE, DELETE

    @Column(length = 10000)
    private String changesJson;

}
