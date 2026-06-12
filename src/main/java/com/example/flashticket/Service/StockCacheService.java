package com.example.flashticket.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Redis 庫存預扣層: 搶票流量先打這裡, 售完/重複的請求完全不碰 MySQL。
 */
@Service
@RequiredArgsConstructor
public class StockCacheService {

    public static final long DEDUCT_SUCCESS_MIN = 0;  // >=0 成功
    public static final long SOLD_OUT = -1;
    public static final long DUPLICATE_USER = -2;

    private final StringRedisTemplate redisTemplate;
    // 欄位名稱須與 RedisConfig 的 bean 名稱一致, Spring 才能區分兩個同型別的 script
    private final DefaultRedisScript<Long> seckillScript;
    private final DefaultRedisScript<Long> seckillRollbackScript;

    private static String stockKey(Long ticketId) {
        return "seckill:stock:" + ticketId;
    }

    private static String usersKey(Long ticketId) {
        return "seckill:users:" + ticketId;
    }

    /** 把 DB 的剩餘庫存載入 Redis, 並清除舊的搶購名單 (重新同步用) */
    public void preloadStock(Long ticketId, int availableStock) {
        redisTemplate.opsForValue().set(stockKey(ticketId), String.valueOf(availableStock));
        redisTemplate.delete(usersKey(ticketId));
    }

    /** Lua 原子扣減, 回傳 -2 重複 / -1 售完 / >=0 剩餘庫存 */
    public long tryDeduct(Long ticketId, Long userId) {
        Long result = redisTemplate.execute(seckillScript,
                List.of(stockKey(ticketId), usersKey(ticketId)), String.valueOf(userId));
        return result == null ? SOLD_OUT : result;
    }

    /** DB 下單失敗時的補償: 還原庫存並把使用者移出名單 */
    public void rollback(Long ticketId, Long userId) {
        redisTemplate.execute(seckillRollbackScript,
                List.of(stockKey(ticketId), usersKey(ticketId)), String.valueOf(userId));
    }

    /** 查詢使用者是否已在搶購名單中 (訂單結果輪詢用) */
    public boolean hasUser(Long ticketId, Long userId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(usersKey(ticketId), String.valueOf(userId)));
    }
}
