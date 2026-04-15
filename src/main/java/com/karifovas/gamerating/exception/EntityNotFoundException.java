package com.karifovas.gamerating.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EntityNotFoundException extends RuntimeException {
    private final String entityClass;
    
    public EntityNotFoundException(String entityClass, String message) {
        super(message);
        this.entityClass = entityClass;
    }
}
