package com.example.Documentation.repository;

import com.example.Documentation.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;


@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query(nativeQuery = true, value = """
        select * from document
        where (:fromDate is null or created_at >= CAST(:fromDate AS DATE))
        and (:toDate is null or created_at <= CAST(:toDate AS DATE))
        """)
    Page<Document> getDocumentsByDateRange(@Param("fromDate") Date fromDate,
                                           @Param("toDate") Date toDate, Pageable pageable);

    @Query(nativeQuery = true, value = """
        select * from document
        where (:fromDate is null or created_at >= CAST(:fromDate AS DATE))
        and (:toDate is null or created_at <= CAST(:toDate AS DATE))
        """)
    List<Document> getDocumentsByDateRange(@Param("fromDate") Date fromDate,
                                           @Param("toDate") Date toDate);

}
