package com.autodiag.AutoDiagIA.entity;


import java.time.Instant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "logs",
    indexes = {
        @Index(
            name = "idx_logs_timestamp",
            columnList = "session_id, timestamp"
        )
    }
)
@Getter
@Setter
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    private String pid;

    private String valueName;

    private Double value;

    private String unit;

    private Instant timestamp;
}