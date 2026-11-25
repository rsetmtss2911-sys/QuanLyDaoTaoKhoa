package com.qldtkhoa.qldtkhoa_app.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
