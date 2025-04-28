package com.example.Documentation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String entityName;
    private Long entityId;
    private String operation;
    private String username;
    private LocalDateTime timestamp;
    @Lob
    private String oldValue;
    @Lob
    private String newValue;
}
