package com.example.flashticket.mq;

import com.example.flashticket.Service.StockCacheService;
import com.example.flashticket.config.RabbitMQConfig;
import com.example.flashticket.dto.OrderMessage;
import com.example.flashticket.entity.OrderStatus;
import com.example.flashticket.entity.TicketOrder;
import com.example.flashticket.repository.TicketOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

/**
 * 死信佇列補償: 下單重試耗盡後, 把 Redis 庫存還給其他人,
 * 並寫入 FAILED 訂單讓輪詢端點能回報失敗。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDlqConsumer {

    private final StockCacheService stockCacheService;
    private final TicketOrderRepository orderRepository;

    @RabbitListener(queues = RabbitMQConfig.DLQ)
    public void handle(OrderMessage msg) {
        log.error("下單最終失敗, 進行補償: {}", msg);

        // 冪等: 已有訂單 (不論成敗) 就不再補償
        if (orderRepository.existsByUserIdAndCampaignTicketId(msg.userId(), msg.ticketId()))
            return;

        stockCacheService.restoreStockOnly(msg.ticketId());

        TicketOrder order = new TicketOrder();
        order.setOrderNo(msg.orderNo());
        order.setUserId(msg.userId());
        order.setCampaignTicketId(msg.ticketId());
        order.setStatus(OrderStatus.FAILED);
        try {
            orderRepository.save(order);
        } catch (DataIntegrityViolationException e) {
            log.info("補償時訂單已存在, 略過: {}", msg.orderNo());
        }
    }
}
