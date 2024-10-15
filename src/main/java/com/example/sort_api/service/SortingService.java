package com.example.sort_api.service;

import com.example.sort_api.entity.SortingLog;
import com.example.sort_api.entity.enums.SortingAlgorithm;
import com.example.sort_api.controller.SortingRequest;
import com.example.sort_api.repository.SortingLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SortingService {

    private static final Logger logger = LoggerFactory.getLogger(SortingService.class);

    private final RestTemplate restTemplate;
    private final Random random = new Random();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(); // Однопоточный Executor

    @Value("#{'${service.instance.urls}'.split(',')}")
    private List<String> otherInstanceUrls;

    private long responseDelayMean;
    private double errorProbability;

    @Autowired
    private SortingLogRepository sortingLogRepository;

    @Value("${response.delay.mean}")
    public void setResponseDelayMean(long responseDelayMean) {
        this.responseDelayMean = responseDelayMean;
    }

    @Value("${response.error.probability}")
    public void setErrorProbability(double errorProbability) {
        this.errorProbability = errorProbability;
    }

    public SortingService(@Lazy RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public CompletableFuture<int[]> sortAsync(int[] numbers, SortingAlgorithm algorithm) {
        return CompletableFuture.supplyAsync(() -> sort(numbers, algorithm), executor);
    }

    public synchronized int[] sort(int[] numbers, SortingAlgorithm algorithm) {
        long startTime = System.currentTimeMillis();
        boolean errorOccurred = false;
        int[] sortedNumbers;

        logger.info("Sorting started with algorithm: {}", algorithm);

        try {
            sortedNumbers = performSorting(numbers, algorithm);
            logger.info("Sorting completed with algorithm: {}", algorithm);

            simulateDelay();

            if (shouldSimulateError()) {
                throw new RuntimeException("Simulated internal server error.");
            }
        } catch (Exception e) {
            errorOccurred = true;
            logger.error("An error occurred during sorting: {}", e.getMessage());
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            SortingLog log = new SortingLog();
            log.setAlgorithm(algorithm);
            log.setArraySize(numbers.length);
            log.setExecutionTime(executionTime);
            log.setErrorOccurred(errorOccurred);

            sortingLogRepository.save(log);
        }

        sendToInstances(numbers, algorithm);
        return sortedNumbers;
    }

    private int[] performSorting(int[] numbers, SortingAlgorithm algorithm) {
        logger.info("Performing {} sort...", algorithm);
        switch (algorithm) {
            case BUBBLE_SORT:
                return bubbleSort(numbers);
            case INSERTION_SORT:
                return insertionSort(numbers);
            default:
                throw new IllegalArgumentException("Unsupported sorting algorithm: " + algorithm);
        }
    }

    private int[] bubbleSort(int[] numbers) {
        logger.info("Starting Bubble Sort on array of size {}", numbers.length);
        int n = numbers.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (numbers[j] > numbers[j + 1]) {
                    int temp = numbers[j];
                    numbers[j] = numbers[j + 1];
                    numbers[j + 1] = temp;
                }
            }
        }
        return numbers;
    }

    private int[] insertionSort(int[] numbers) {
        logger.info("Starting Insertion Sort on array of size {}", numbers.length);
        int n = numbers.length;
        for (int i = 1; i < n; i++) {
            int key = numbers[i];
            int j = i - 1;

            while (j >= 0 && numbers[j] > key) {
                numbers[j + 1] = numbers[j];
                j = j - 1;
            }
            numbers[j + 1] = key;
        }
        return numbers;
    }

    public void sendToInstances(int[] numbers, SortingAlgorithm algorithm) {
        for (String serviceUrl : otherInstanceUrls) {
            if (serviceUrl == null || serviceUrl.isEmpty()) {
                logger.warn("Skipping service, as the URL is missing or empty.");
                continue;
            }

            try {
                String url = serviceUrl + "/api/sort";
                SortingRequest sortingRequest = new SortingRequest();
                sortingRequest.setArray(numbers);
                sortingRequest.setAlgorithm(algorithm);

                logger.info("Sending request to service at {} with array: {}", url, Arrays.toString(numbers));
                ResponseEntity<Void> response = restTemplate.postForEntity(url, sortingRequest, Void.class);

                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Request successfully sent to service at {}", url);
                } else {
                    logger.warn("Failed to send request to service at {}. Status code: {}", url, response.getStatusCode());
                }
            } catch (Exception e) {
                logger.error("Error while sending request to service at {}: {}", serviceUrl, e.getMessage());
            }
        }
    }

    private void simulateDelay() {
        long delay = 0;
        try {
            delay = (long) (responseDelayMean + random.nextGaussian() * responseDelayMean * 0.5);
            delay = Math.max(delay, 0);
            logger.info("Simulating delay of {} ms before processing the request.", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            logger.warn("Delay simulation interrupted: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private boolean shouldSimulateError() {
        double chance = random.nextDouble();
        logger.info("Simulating failure check with probability: {} (Generated value: {})", errorProbability, chance);
        return chance < errorProbability;
    }
}
