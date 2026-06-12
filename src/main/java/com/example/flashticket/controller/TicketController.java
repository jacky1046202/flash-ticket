package com.example.flashticket.controller;

import com.example.flashticket.Service.StockPreloader;
import com.example.flashticket.Service.TicketService;
import com.example.flashticket.dto.ApiResponse;
import com.example.flashticket.entity.CampaignTicket;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final StockPreloader stockPreloader;

    /** 搶票: 通過 Redis 扣減後即回 202 排隊中, 實際下單由 MQ 消費者非同步完成 */
    @PostMapping("/buy")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<String> buyTicket(@RequestParam("ticketID") Long ticketID, @RequestParam("userID") Long userID){
        return ApiResponse.ok(ticketService.buyTicket(ticketID, userID));
    }

    @GetMapping()
    public ApiResponse<List<CampaignTicket>> showTickets(){
        return ApiResponse.ok(ticketService.showTickets());
    }

    /** 手動把 DB 庫存重新同步到 Redis (活動上架/修改庫存後使用) */
    @PostMapping("/{id}/preload")
    public ApiResponse<String> preloadStock(@PathVariable("id") Long id){
        stockPreloader.preload(id);
        return ApiResponse.ok("庫存已同步至 Redis");
    }
}
