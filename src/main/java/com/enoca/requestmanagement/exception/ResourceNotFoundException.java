package com.enoca.requestmanagement.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String resource, Object identifier) {
        return new ResourceNotFoundException(resource + " bulunamadi: " + identifier);
    }
}
