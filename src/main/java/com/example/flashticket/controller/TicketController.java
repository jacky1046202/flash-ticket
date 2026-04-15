package com.example.flashticket.controller;

import com.example.flashticket.Service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/buy")
    private String butTicket(@RequestParam Long ticketID, @RequestParam Long userID){
        try{
            return ticketService.buyTicket(ticketID, userID);
        }catch (Exception e){
            return "搶票失敗：" + e.getMessage();
        }
    }
}
