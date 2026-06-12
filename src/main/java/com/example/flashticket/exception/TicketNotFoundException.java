package com.example.flashticket.exception;

public class TicketNotFoundException extends BusinessException {
    public TicketNotFoundException(Long ticketId) {
        super("找不到該票券, id:" + ticketId);
    }
}
