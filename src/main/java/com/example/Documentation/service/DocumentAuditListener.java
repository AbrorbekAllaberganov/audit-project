package com.example.Documentation.service;

import com.example.Documentation.entity.Document;
import com.example.Documentation.entity.DocumentChangeLog;
import com.example.Documentation.repository.DocumentChangeLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DocumentAuditListener {
    private final DocumentChangeLogRepository documentChangeLogRepository;

    @PrePersist
    public void auditInsert(Document doc) {
         Map<String, Object[]> diff = new LinkedHashMap<>();
        extractFields(null, doc, diff);
        saveAudit(doc.getId(), getUsername(), "CREATE", diff);
    }

    @PreUpdate
    public void auditUpdate(Document doc) {
        System.out.println("update doc");
        Document original = (Document) doc.getOriginalState();
        Map<String, Object[]> diffs = getDifferences(original, doc);
        saveAudit(doc.getId(), getUsername(), "UPDATE", diffs);
    }

    @PreRemove
    public void auditDelete(Document doc) {
        Map<String, Object[]> diff = new LinkedHashMap<>();
        extractFields(doc, null, diff);
        saveAudit(doc.getId(), getUsername(), "DELETE", diff);
    }

    private void extractFields(Object oldObj, Object newObj, Map<String, Object[]> diff) {
        Object target = oldObj != null ? oldObj : newObj;
        for (Field field : target.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldVal = oldObj != null ? field.get(oldObj) : null;
                Object newVal = newObj != null ? field.get(newObj) : null;
                diff.put(field.getName(), new Object[]{oldVal, newVal});
            } catch (IllegalAccessException ignored) {}
        }
    }

    private Map<String, Object[]> getDifferences(Object oldObj, Object newObj) {
        Map<String, Object[]> changes = new LinkedHashMap<>();
        for (Field field : oldObj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldVal = field.get(oldObj);
                Object newVal = field.get(newObj);
                if (!Objects.equals(oldVal, newVal)) {
                    changes.put(field.getName(), new Object[]{oldVal, newVal});
                }
            } catch (IllegalAccessException ignored) {}
        }
        return changes;
    }

    private String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private void saveAudit(Long docId, String user, String operation, Map<String, Object[]> diffs) {
        DocumentChangeLog log = new DocumentChangeLog();
        log.setDocumentId(docId);
        log.setUsername(user);
        log.setTimestamp(LocalDateTime.now());
        log.setOperation(operation);

        try {
            log.setChangesJson(new ObjectMapper().writeValueAsString(diffs));
        } catch (JsonProcessingException e) {
            log.setChangesJson("{\"error\": \"Could not serialize changes\"}");
        }

        documentChangeLogRepository.save(log);
    }
}