package com.example.Documentation.repository;

import com.example.Documentation.entity.DocumentChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentChangeLogRepository extends JpaRepository<DocumentChangeLog, Long> {
}
