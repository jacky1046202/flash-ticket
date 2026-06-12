package com.example.flashticket;

import com.example.flashticket.Service.StockCacheService;
import com.example.flashticket.Service.TicketService;
import com.example.flashticket.entity.CampaignTicket;
import com.example.flashticket.exception.DuplicateOrderException;
import com.example.flashticket.exception.SoldOutException;
import com.example.flashticket.repository.CampaignTicketRepository;
import com.example.flashticket.repository.TicketOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ConcurrentBuyTest {

    private static final int STOCK = 100;
    private static final int THREADS = 200;

    @Autowired
    private TicketService ticketService;
    @Autowired
    private CampaignTicketRepository ticketRepository;
    @Autowired
    private TicketOrderRepository orderRepository;
    @Autowired
    private StockCacheService stockCacheService;

    private Long ticketId;

    @BeforeEach
    void setUp() {
        CampaignTicket ticket = new CampaignTicket();
        ticket.setCampaignName("併發測試活動");
        ticket.setTotalStock(STOCK);
        ticket.setAvailableStock(STOCK);
        ticket.setStartTime(LocalDateTime.now().minusMinutes(5));
        ticket.setEndTime(LocalDateTime.now().plusMinutes(5));
        ticketId = ticketRepository.save(ticket).getId();
        stockCacheService.preloadStock(ticketId, STOCK); // 搶票第一道防線在 Redis, 須先預熱
    }

    /** 200 個不同使用者同時搶 100 張票: 恰好成功 100 次, 不超賣 */
    @Test
    void noOversellWhenManyUsersBuyConcurrently() throws InterruptedException {
        AtomicInteger success = new AtomicInteger();
        AtomicInteger soldOut = new AtomicInteger();

        runConcurrently(i -> {
            try {
                ticketService.buyTicket(ticketId, (long) (i + 1));
                success.incrementAndGet();
            } catch (SoldOutException e) {
                soldOut.incrementAndGet();
            }
        });

        assertEquals(STOCK, success.get());
        assertEquals(THREADS - STOCK, soldOut.get());
        assertEquals(0, ticketRepository.findById(ticketId).orElseThrow().getAvailableStock());
        assertEquals(STOCK, orderRepository.countByCampaignTicketId(ticketId));
    }

    /** 同一個使用者同時搶 200 次: 只會有 1 張訂單 */
    @Test
    void sameUserOnlyGetsOneOrder() throws InterruptedException {
        AtomicInteger success = new AtomicInteger();

        runConcurrently(i -> {
            try {
                ticketService.buyTicket(ticketId, 1L);
                success.incrementAndGet();
            } catch (DuplicateOrderException ignored) {
            }
        });

        assertEquals(1, success.get());
        assertEquals(1, orderRepository.countByCampaignTicketId(ticketId));
        assertEquals(STOCK - 1, ticketRepository.findById(ticketId).orElseThrow().getAvailableStock());
    }

    /** 用 CountDownLatch 讓所有執行緒同一瞬間出發, 製造最大競爭 */
    private void runConcurrently(java.util.function.IntConsumer action) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch ready = new CountDownLatch(THREADS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            int idx = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    action.accept(idx);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();
        start.countDown();
        done.await();
        pool.shutdown();
    }
}
