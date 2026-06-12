package com.example.flashticket.exception;

public class DuplicateOrderException extends BusinessException {
    public DuplicateOrderException() {
        super("已搶購過該票券, 一人限購一張");
    }
}
