package com.example.flashticket.controller;

import com.example.flashticket.Service.TicketService;
import com.example.flashticket.dto.ApiResponse;
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
    public ApiResponse<String> buyTicket(@RequestParam("ticketID") Long ticketID, @RequestParam("userID") Long userID){
        return ApiResponse.ok(ticketService.buyTicket(ticketID, userID));
    }

    @GetMapping()
    public ApiResponse<List<CampaignTicket>> showTickets(){
        return ApiResponse.ok(ticketService.showTickets());
    }
}
