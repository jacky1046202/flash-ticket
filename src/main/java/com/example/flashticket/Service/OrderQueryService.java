package com.example.flashticket.Service;

import com.example.flashticket.dto.OrderResult;
import com.example.flashticket.repository.TicketOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final TicketOrderRepository orderRepository;
    private final StockCacheService stockCacheService;

    public OrderResult getResult(Long userId, Long ticketId) {
        return orderRepository.findByUserIdAndCampaignTicketId(userId, ticketId)
                .map(order -> new OrderResult(order.getStatus().name(), order.getOrderNo()))
                .orElseGet(() -> stockCacheService.hasUser(ticketId, userId)
                        ? new OrderResult("QUEUING", null)   // 已過 Redis 扣減, 訂單還在 MQ 排隊
                        : new OrderResult("NONE", null));
    }
}
