package com.example.flashticket.dto;

/** 下單訊息: orderNo 在發送端生成, 客戶端立即取得, 消費端以唯一欄位天然冪等 */
public record OrderMessage(String orderNo, Long ticketId, Long userId) {
}
