# 壓測指南 (JMeter)

## 場景

500 個不同使用者在 5 秒內湧入，搶購同一檔活動的票券。
合法回應：`202` 排隊中、`409` 售完/重複、`429` 被限流。

## 前置準備

1. 啟動依賴與應用：
   ```powershell
   docker compose up -d
   .\mvnw.cmd spring-boot:run
   ```
2. 重置測試活動（庫存 100），記下回傳的 id：
   ```sql
   INSERT INTO campaign_ticket (campaign_name, total_stock, available_stock, start_time, end_time, version)
   VALUES ('壓測活動', 100, 100, NOW() - INTERVAL 1 HOUR, NOW() + INTERVAL 1 DAY, 0);
   ```
3. 把該活動庫存同步進 Redis：
   ```powershell
   Invoke-WebRequest -Method POST "http://localhost:8080/api/tickets/{id}/preload"
   ```

## 執行

```powershell
cd load-test
jmeter -n -t seckill.jmx -JticketId={id} -l result.jtl -e -o report
```

跑完開 `report\index.html` 看吞吐量與 p99 延遲。
（沒裝 JMeter：https://jmeter.apache.org/download_jmeter.cgi 下載後把 bin 加入 PATH）

## 驗證不超賣

```sql
SELECT available_stock FROM campaign_ticket WHERE id = {id};                          -- 預期 0
SELECT COUNT(*) FROM ticket_order WHERE campaign_ticket_id = {id} AND status='SUCCESS'; -- 預期恰好 100
```

## 前後對比

每個階段都有 git tag，可以切換版本跑同一份壓測比較：

```powershell
git checkout stage-1   # 純 DB 同步下單
git checkout stage-3   # Redis 預扣 + MQ 非同步
git checkout stage-4   # + 限流
```

重點觀察：`/buy` 的吞吐量 (Throughput) 與 p99 延遲。stage-3 之後 API 只碰
Redis + MQ 發送，p99 應顯著低於 stage-1；售完後的請求完全不會進 MySQL。

也可在壓測時觀察：
- RabbitMQ 佇列堆積：http://localhost:15672 (guest/guest)
- 延遲百分位：http://localhost:8080/actuator/metrics/http.server.requests
