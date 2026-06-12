package com.example.flashticket.mq;

import com.example.flashticket.Service.OrderService;
import com.example.flashticket.config.RabbitMQConfig;
import com.example.flashticket.dto.OrderMessage;
import com.example.flashticket.exception.DuplicateOrderException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderMessageConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handle(OrderMessage msg) {
        try {
            // placeOrder 內含原子扣減 (DB 最終防線) 與唯一約束
            orderService.placeOrder(msg.orderNo(), msg.ticketId(), msg.userId());
        } catch (DuplicateOrderException e) {
            // 訊息重複投遞時的冪等處理: 訂單已存在, 直接略過
            log.info("訂單已存在, 略過重複訊息: {}", msg.orderNo());
        }
        // 其他例外 (如 SoldOutException) 往外拋 → 重試 3 次 → 進 DLQ
    }
}
