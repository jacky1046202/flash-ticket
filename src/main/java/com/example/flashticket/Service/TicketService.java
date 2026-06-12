package com.example.flashticket.Service;

import com.example.flashticket.entity.CampaignTicket;
import com.example.flashticket.entity.OrderStatus;
import com.example.flashticket.entity.TicketOrder;
import com.example.flashticket.exception.CampaignNotActiveException;
import com.example.flashticket.exception.DuplicateOrderException;
import com.example.flashticket.exception.SoldOutException;
import com.example.flashticket.exception.TicketNotFoundException;
import com.example.flashticket.repository.CampaignTicketRepository;
import com.example.flashticket.repository.TicketOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketOrderRepository orderRepository;
    private final CampaignTicketRepository ticketRepository;

    @Transactional
    public String buyTicket(Long ticketId, Long userId){
        CampaignTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        LocalDateTime now = LocalDateTime.now();
        if(now.isBefore(ticket.getStartTime()) || now.isAfter(ticket.getEndTime()))
            throw new CampaignNotActiveException();

        // 快速預檢, 真正的「一人一單」保證是 DB 的唯一約束 uk_user_campaign
        if(orderRepository.existsByUserIdAndCampaignTicketId(userId, ticketId))
            throw new DuplicateOrderException();

        // 原子 UPDATE 防超賣: 回傳 0 表示已售完
        if(ticketRepository.deductStock(ticketId) == 0)
            throw new SoldOutException();

        TicketOrder order = new TicketOrder();
        order.setOrderNo(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setCampaignTicketId(ticketId);
        order.setStatus(OrderStatus.SUCCESS);

        try{
            // flush 讓唯一約束違反在此立即拋出, 交易回滾時連同上面的扣減一起還原
            orderRepository.saveAndFlush(order);
        }catch (DataIntegrityViolationException e){
            throw new DuplicateOrderException();
        }

        return "搶票成功, 訂單編號:" + order.getOrderNo();
    }

    public List<CampaignTicket> showTickets(){
        return ticketRepository.findAll();
    }
}
