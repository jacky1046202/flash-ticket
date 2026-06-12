package com.example.flashticket.dto;

/** 訂單結果輪詢回應: status = SUCCESS / FAILED / QUEUING / NONE */
public record OrderResult(String status, String orderNo) {
}
