package com.qldtkhoa.qldtkhoa_app.exception;

public class DuplicateResourceException extends DomainException {
    public DuplicateResourceException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
