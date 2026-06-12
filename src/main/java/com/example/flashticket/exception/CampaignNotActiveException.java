package com.example.flashticket.exception;

public class CampaignNotActiveException extends BusinessException {
    public CampaignNotActiveException() {
        super("不在活動時間內");
    }
}
