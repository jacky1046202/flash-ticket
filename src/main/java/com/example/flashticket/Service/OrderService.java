package com.example.flashticket.Service;

import com.example.flashticket.entity.OrderStatus;
import com.example.flashticket.entity.TicketOrder;
import com.example.flashticket.exception.DuplicateOrderException;
import com.example.flashticket.exception.SoldOutException;
import com.example.flashticket.repository.CampaignTicketRepository;
import com.example.flashticket.repository.TicketOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final TicketOrderRepository orderRepository;
    private final CampaignTicketRepository ticketRepository;

    /**
     * DB 落單 (Stage 1 的交易邏輯): 原子扣減 + 唯一約束是最終防線。
     * 獨立成類別是因為 @Transactional 自我呼叫不會經過代理。
     */
    @Transactional
    public TicketOrder placeOrder(String orderNo, Long ticketId, Long userId) {
        // 快速預檢, 真正的「一人一單」保證是 DB 的唯一約束 uk_user_campaign
        if (orderRepository.existsByUserIdAndCampaignTicketId(userId, ticketId))
            throw new DuplicateOrderException();

        // 原子 UPDATE 防超賣: 回傳 0 表示已售完
        if (ticketRepository.deductStock(ticketId) == 0)
            throw new SoldOutException();

        TicketOrder order = new TicketOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setCampaignTicketId(ticketId);
        order.setStatus(OrderStatus.SUCCESS);

        try {
            // flush 讓唯一約束違反在此立即拋出, 交易回滾時連同上面的扣減一起還原
            orderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateOrderException();
        }
        return order;
    }
}
