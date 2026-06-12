package com.example.flashticket.controller;

import com.example.flashticket.Service.OrderQueryService;
import com.example.flashticket.dto.ApiResponse;
import com.example.flashticket.dto.OrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderQueryService orderQueryService;

    /** 搶票後輪詢下單結果 */
    @GetMapping("/result")
    public ApiResponse<OrderResult> getResult(@RequestParam("userID") Long userID,
                                              @RequestParam("ticketID") Long ticketID) {
        return ApiResponse.ok(orderQueryService.getResult(userID, ticketID));
    }
}
