package com.example.flashticket.repository;

import com.example.flashticket.entity.CampaignTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CampaignTicketRepository extends JpaRepository<CampaignTicket, Long> {

    /**
     * 原子扣減庫存: 判斷與扣減在同一條 UPDATE 完成, 由資料庫的行鎖保證不超賣。
     * 回傳 1 = 扣減成功, 0 = 已售完。
     */
    @Modifying
    @Query("UPDATE CampaignTicket t SET t.availableStock = t.availableStock - 1 " +
           "WHERE t.id = :id AND t.availableStock > 0")
    int deductStock(@Param("id") Long id);
}
