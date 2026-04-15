package com.example.flashticket.repository;

import com.example.flashticket.entity.CampaignTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignTicketRepository extends JpaRepository<CampaignTicket, Long> {
}
