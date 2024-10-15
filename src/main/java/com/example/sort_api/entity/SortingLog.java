package com.example.sort_api.entity;

import com.example.sort_api.entity.enums.SortingAlgorithm;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "sorting_logs")
@Data
public class SortingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private SortingAlgorithm algorithm;
    private int arraySize;
    private long executionTime;
    private LocalDateTime timestamp = LocalDateTime.now();
    private boolean errorOccurred;
}

