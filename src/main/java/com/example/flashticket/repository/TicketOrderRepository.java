package com.example.flashticket.repository;

import com.example.flashticket.entity.TicketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketOrderRepository extends JpaRepository<TicketOrder, Long> {

    boolean existsByUserIdAndCampaignTicketId(Long userId, Long campaignTicketId);

    Optional<TicketOrder> findByUserIdAndCampaignTicketId(Long userId, Long campaignTicketId);

    long countByCampaignTicketId(Long campaignTicketId);
}
