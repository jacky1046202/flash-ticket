package com.example.flashticket.config;

import com.example.flashticket.dto.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 搶票介面限流 (token bucket):
 * - 全域桶保護後端 (Redis/MySQL) 不被瞬間流量打垮
 * - 每用戶桶防止單一使用者用腳本洗請求
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    // 全域: 容量 200, 每秒補充 100 個 token
    private final Bucket globalBucket = Bucket.builder()
            .addLimit(limit -> limit.capacity(200).refillGreedy(100, Duration.ofSeconds(1)))
            .build();

    // 每用戶: 容量 2, 每秒補充 1 個 token
    // 注意: 此 Map 不會過期清理, 僅適合學習/單機; 生產環境應換成 Caffeine 等帶過期的快取
    private final ConcurrentHashMap<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String userId = request.getParameter("userID");
        Bucket userBucket = userBuckets.computeIfAbsent(userId == null ? "anonymous" : userId,
                k -> Bucket.builder()
                        .addLimit(limit -> limit.capacity(2).refillGreedy(1, Duration.ofSeconds(1)))
                        .build());

        if (userBucket.tryConsume(1) && globalBucket.tryConsume(1))
            return true;

        response.setStatus(429);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiResponse.fail(429, "請求過於頻繁, 請稍後再試")));
        return false;
    }
}
