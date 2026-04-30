package com.example.flashticket.controller;

import com.example.flashticket.Service.TicketService;
import com.example.flashticket.entity.CampaignTicket;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/buy")
    private String buyTicket(@RequestParam("ticketID") Long ticketID, @RequestParam("userID") Long userID){
        try{
            return ticketService.buyTicket(ticketID, userID);
        }catch (Exception e){
            return "搶票失敗：" + e.getMessage();
        }
    }

    @GetMapping()
    private List<CampaignTicket> showTickets(){
        return ticketService.showTickets();
    }
}
