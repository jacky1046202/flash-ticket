package com.example.flashticket.Service;

import com.example.flashticket.entity.CampaignTicket;
import com.example.flashticket.exception.TicketNotFoundException;
import com.example.flashticket.repository.CampaignTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 庫存預熱: 啟動時把進行中活動的 DB 庫存載入 Redis。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockPreloader implements ApplicationRunner {

    private final CampaignTicketRepository ticketRepository;
    private final StockCacheService stockCacheService;

    @Override
    public void run(ApplicationArguments args) {
        LocalDateTime now = LocalDateTime.now();
        for (CampaignTicket ticket : ticketRepository.findAll()) {
            if (!now.isBefore(ticket.getStartTime()) && !now.isAfter(ticket.getEndTime())) {
                stockCacheService.preloadStock(ticket.getId(), ticket.getAvailableStock());
                log.info("庫存預熱: 活動 {} (id={}) 載入 Redis, 庫存 {}",
                        ticket.getCampaignName(), ticket.getId(), ticket.getAvailableStock());
            }
        }
    }

    /** 手動重新同步單一活動的庫存 (管理端點用) */
    public void preload(Long ticketId) {
        CampaignTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        stockCacheService.preloadStock(ticketId, ticket.getAvailableStock());
        log.info("庫存重新同步: 活動 id={}, 庫存 {}", ticketId, ticket.getAvailableStock());
    }
}
