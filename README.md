# Flash Ticket 搶票系統

Flash Ticket 是一個基於 Spring Boot 的搶票系統後端服務，支援高併發場景下的票券搶購功能，透過樂觀鎖機制確保庫存的一致性與正確性。

## 技術架構

- **Java 17**
- **Spring Boot 3.5.13**
- **Spring Data JPA / Hibernate** — ORM 與資料存取
- **MySQL** — 關聯式資料庫
- **Lombok** — 減少樣板程式碼
- **Bean Validation** — 請求參數驗證

## 專案結構

```
src/main/java/com/example/flashticket/
├── FlashticketApplication.java        # 應用程式進入點
├── controller/
│   └── TicketController.java          # REST API 控制器
├── Service/
│   └── TicketService.java             # 搶票核心業務邏輯
├── entity/
│   ├── CampaignTicket.java            # 票券活動實體
│   └── TicketOrder.java               # 訂單實體
└── repository/
    ├── CampaignTicketRepository.java  # 票券活動資料存取
    └── TicketOrderRepository.java     # 訂單資料存取
```

## 功能特色

- **搶票下單** — 使用者可對指定票券活動進行搶購
- **樂觀鎖（Optimistic Locking）** — 透過 `@Version` 欄位防止超賣，確保併發安全
- **交易管理** — 使用 `@Transactional` 確保搶票流程的原子性
- **庫存管理** — 即時檢查與扣減可用庫存

## API 文件

### 搶票

```
POST /api/tickets/buy?ticketID={ticketID}&userID={userID}
```

**參數說明：**

| 參數       | 類型   | 說明       |
|------------|--------|------------|
| `ticketID` | Long   | 票券活動 ID |
| `userID`   | String | 使用者 ID   |

**回應範例：**

- 成功：`搶票成功，訂單編號：ORD-xxxxxxxx`
- 失敗（無庫存）：`很抱歉，票已售完`
- 失敗（系統繁忙）：`系統繁忙，請稍後再試`

## 資料庫結構

### campaign_ticket（票券活動）

| 欄位              | 類型         | 說明               |
|-------------------|--------------|--------------------|
| `id`              | BIGINT (PK)  | 主鍵               |
| `campaign_name`   | VARCHAR      | 活動名稱           |
| `total_stock`     | INT          | 總庫存             |
| `available_stock` | INT          | 可用庫存           |
| `start_time`      | DATETIME     | 活動開始時間       |
| `end_time`        | DATETIME     | 活動結束時間       |
| `version`         | INT          | 樂觀鎖版本號       |

### ticket_order（訂單）

| 欄位                  | 類型         | 說明               |
|-----------------------|--------------|--------------------|
| `id`                  | BIGINT (PK)  | 主鍵               |
| `order_no`            | VARCHAR (UK) | 訂單編號（唯一）   |
| `campaign_ticket_id`  | BIGINT       | 關聯票券活動 ID    |
| `user_id`             | VARCHAR      | 使用者 ID          |
| `status`              | VARCHAR      | 訂單狀態（預設 PENDING） |
| `created_at`          | DATETIME     | 建立時間           |
| `updated_at`          | DATETIME     | 更新時間           |

## 快速開始

### 環境需求

- Java 17+
- MySQL 8.0+
- Maven 3.6+

### 安裝步驟

1. **Clone 專案**

   ```bash
   git clone https://github.com/jacky1046202/flash-ticket.git
   cd flash-ticket
   ```

2. **建立資料庫**

   ```sql
   CREATE DATABASE flash_ticket;
   ```

3. **設定資料庫連線**

   修改 `src/main/resources/application.yml` 中的資料庫帳號密碼：

   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/flash_ticket?serverTimezone=Asia/Taipei&useSSL=false
       username: your_username
       password: your_password
   ```

4. **啟動應用程式**

   ```bash
   ./mvnw spring-boot:run
   ```

   應用程式將在 `http://localhost:8080` 啟動。

### 測試搶票

```bash
curl -X POST "http://localhost:8080/api/tickets/buy?ticketID=1&userID=user001"
```

## License

本專案僅供學習與參考使用。
