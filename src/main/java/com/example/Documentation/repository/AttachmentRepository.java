package com.example.Documentation.repository;

import com.example.Documentation.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.config.annotation.web.oauth2.resourceserver.OpaqueTokenDsl;

import java.util.Optional;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    Optional<Attachment> findByHashId(String hashId);
    boolean existsByHashId (String hashId);
}
