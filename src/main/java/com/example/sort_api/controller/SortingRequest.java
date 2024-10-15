package com.example.sort_api.controller;

import com.example.sort_api.entity.enums.SortingAlgorithm;
import lombok.Data;

@Data
public class SortingRequest {
    private int[] array;
    private SortingAlgorithm algorithm;
}