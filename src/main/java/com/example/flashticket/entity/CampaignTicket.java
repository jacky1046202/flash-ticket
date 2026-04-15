package com.example.flashticket.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "campaign_ticket")
public class CampaignTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, columnDefinition = "VARCHAR(100) COMMENT '活動名稱'")
    private String campaignName;

    @Column(nullable = false, columnDefinition = "INT COMMENT '總庫存'")
    private Integer totalStock;

    @Column(nullable = false, columnDefinition = "INT COMMENT '總庫存'")
    private Integer availableStock;

    @Column(nullable = false, columnDefinition = "DateTime COMMENT '開始時間'")
    private LocalDateTime startTime;

    @Column(nullable = false, columnDefinition = "DateTime COMMENT '結束時間'")
    private LocalDateTime endTime;

    @Version
    @Column(nullable = false, columnDefinition = "INT COMMENT '版本'")
    private Integer version;
}
