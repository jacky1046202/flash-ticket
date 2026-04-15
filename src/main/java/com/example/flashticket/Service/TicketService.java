package com.example.flashticket.Service;

import com.example.flashticket.entity.CampaignTicket;
import com.example.flashticket.entity.TicketOrder;
import com.example.flashticket.repository.CampaignTicketRepository;
import com.example.flashticket.repository.TicketOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {
    private final TicketOrderRepository orderRepository;
    private final CampaignTicketRepository ticketRepository;

    @Transactional
    public String buyTicket(Long ticketId, Long userId){
        CampaignTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("找不到該票券"));

        int curStock = ticket.getAvailableStock();
        if(curStock <= 0) throw new RuntimeException("票券已售完");

        ticket.setAvailableStock(--curStock);

        ticketRepository.save(ticket);

        TicketOrder order = new TicketOrder();

        order.setOrderNo(UUID.randomUUID().toString());
        order.setUserId(userId);
        order.setCampaignTicketId(ticketId);

        orderRepository.save(order);

        return "搶票成功, 訂單編號:" + order.getOrderNo();
    }
}
