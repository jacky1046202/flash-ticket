package com.example.flashticket.exception;

public class SoldOutException extends BusinessException {
    public SoldOutException() {
        super("票券已售完");
    }
}
