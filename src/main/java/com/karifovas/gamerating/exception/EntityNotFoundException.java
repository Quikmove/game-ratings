package com.karifovas.gamerating.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EntityNotFoundException extends RuntimeException {
    private final String identifier;
    
    public EntityNotFoundException(String identifier, String message) {
        super(message);
        this.identifier = identifier;
    }
}
