package com.example.flashticket.Service;

import com.example.flashticket.dto.OrderMessage;
import com.example.flashticket.entity.CampaignTicket;
import com.example.flashticket.exception.CampaignNotActiveException;
import com.example.flashticket.exception.DuplicateOrderException;
import com.example.flashticket.exception.SoldOutException;
import com.example.flashticket.exception.TicketNotFoundException;
import com.example.flashticket.mq.OrderMessageProducer;
import com.example.flashticket.repository.CampaignTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final CampaignTicketRepository ticketRepository;
    private final StockCacheService stockCacheService;
    private final OrderMessageProducer orderMessageProducer;

    public String buyTicket(Long ticketId, Long userId){
        CampaignTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(ticket.getStartTime()) || now.isAfter(ticket.getEndTime()))
            throw new CampaignNotActiveException();

        // 第一道防線: Redis Lua 原子扣減, 售完/重複的請求不會碰到 MySQL
        long result = stockCacheService.tryDeduct(ticketId, userId);
        if(result == StockCacheService.DUPLICATE_USER)
            throw new DuplicateOrderException();
        if(result == StockCacheService.SOLD_OUT)
            throw new SoldOutException();

        // 第二步: 丟進 MQ 非同步落單, API 立即回應 (削峰填谷)
        String orderNo = UUID.randomUUID().toString();
        try{
            orderMessageProducer.send(new OrderMessage(orderNo, ticketId, userId));
        }catch (RuntimeException e){
            // 訊息發送失敗 → 補償還原 Redis, 使用者可重試
            stockCacheService.rollback(ticketId, userId);
            throw e;
        }
        return "排隊中, 訂單編號:" + orderNo;
    }

    public List<CampaignTicket> showTickets(){
        return ticketRepository.findAll();
    }
}
