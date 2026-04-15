package com.example.flashticket.repository;

import com.example.flashticket.entity.CampaignTicket;
import com.example.flashticket.entity.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {
}
