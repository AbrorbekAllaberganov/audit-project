package com.example.Documentation.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class AuditableEntity {
    @Transient
    private Object originalState;

    @PostLoad
    public void storeOriginalState() {
        this.originalState = deepCopy(this);
    }

    public Object getOriginalState() {
        return originalState;
    }

    private Object deepCopy(Object entity) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(entity, entity.getClass());
    }

}