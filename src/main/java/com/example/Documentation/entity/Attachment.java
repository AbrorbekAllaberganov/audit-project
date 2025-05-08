package com.example.Documentation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String link;

    String name;

    String hashId;

    String uploadPath;

    String contentType;

    String extension;

    Long fileSize;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Attachment that = (Attachment) obj;
        return Objects.equals(hashId, that.hashId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashId);
    }

}
