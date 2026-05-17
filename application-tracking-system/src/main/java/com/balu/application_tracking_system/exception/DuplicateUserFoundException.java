package com.balu.application_tracking_system.exception;

public class DuplicateUserFoundException extends RuntimeException {
    public DuplicateUserFoundException(String msg) {
        super(msg);
    }
}
