package com.qldtkhoa.qldtkhoa_app.exception;

public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}
