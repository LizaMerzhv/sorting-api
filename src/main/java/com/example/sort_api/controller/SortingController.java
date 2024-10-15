package com.example.sort_api.controller;

import com.example.sort_api.entity.SortingLog;
import com.example.sort_api.repository.SortingLogRepository;
import com.example.sort_api.service.SortingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/sort")
public class SortingController {

    private final SortingService sortingService;
    private final SortingLogRepository sortingLogRepository;

    @Autowired
    public SortingController(@Lazy SortingService sortingService, SortingLogRepository sortingLogRepository) {
        this.sortingService = sortingService;
        this.sortingLogRepository = sortingLogRepository;
    }

    @GetMapping("/all")
    public List<SortingLog> getAllLogs() {
        return sortingLogRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SortingLog> getLogById(@PathVariable Long id) {
        return sortingLogRepository.findById(id)
                .map(log -> ResponseEntity.ok(log))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<int[]>> sortArray(@RequestBody SortingRequest request) {
        return sortingService.sortAsync(request.getArray(), request.getAlgorithm())
                .thenApply(ResponseEntity::ok);
    }

    @PostMapping("/error-probability")
    public ResponseEntity<String> updateErrorProbability(@RequestParam double probability) {
        if (probability < 0 || probability > 1) {
            return ResponseEntity.badRequest().body("Error probability must be between 0 and 1.");
        }
        sortingService.setErrorProbability(probability);
        return ResponseEntity.ok("Error probability updated to " + probability);
    }

    @PostMapping("/response-delay")
    public ResponseEntity<String> updateResponseDelay(@RequestParam long delay) {
        if (delay < 0) {
            return ResponseEntity.badRequest().body("Delay must be a positive number.");
        }
        sortingService.setResponseDelayMean(delay);
        return ResponseEntity.ok("Response delay updated to " + delay + " ms");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        if (sortingLogRepository.existsById(id)) {
            sortingLogRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
