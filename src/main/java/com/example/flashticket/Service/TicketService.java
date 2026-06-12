package com.example.flashticket.Service;

import com.example.flashticket.entity.CampaignTicket;
import com.example.flashticket.exception.CampaignNotActiveException;
import com.example.flashticket.exception.DuplicateOrderException;
import com.example.flashticket.exception.SoldOutException;
import com.example.flashticket.exception.TicketNotFoundException;
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
    private final OrderService orderService;

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

        try{
            String orderNo = UUID.randomUUID().toString();
            orderService.placeOrder(orderNo, ticketId, userId);
            return "搶票成功, 訂單編號:" + orderNo;
        }catch (RuntimeException e){
            // DB 落單失敗 → 補償還原 Redis (最壞情況只會少賣, DB 原子扣減保證絕不超賣)
            stockCacheService.rollback(ticketId, userId);
            throw e;
        }
    }

    public List<CampaignTicket> showTickets(){
        return ticketRepository.findAll();
    }
}
