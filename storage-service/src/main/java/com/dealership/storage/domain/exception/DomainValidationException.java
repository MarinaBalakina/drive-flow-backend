package com.dealership.storage.domain.exception;

public class DomainValidationException extends RuntimeException{
    public DomainValidationException(String message){
        super(message);
    }

    public DomainValidationException(String message, Throwable reason){
        super(message, reason);
    }
}
