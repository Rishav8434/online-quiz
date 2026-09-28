package com.onlinequiz.security;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class RateLimiterService {

    // Tracks request timestamps per clientKey (IP + action)
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> requestCounts = new ConcurrentHashMap<>();

    // Standard rate limit window: 60 seconds (1 minute)
    private static final long TIME_WINDOW_MS = 60_000L;

    // Maximum allowed requests within the time window
    private static final int MAX_REQUESTS_PER_MINUTE = 15;

    public boolean tryAcquire(String key) {
        long currentTime = System.currentTimeMillis();

        ConcurrentLinkedQueue<Long> timestamps = requestCounts.computeIfAbsent(key, k -> new ConcurrentLinkedQueue<>());

        // Evict timestamps older than 1 minute
        while (!timestamps.isEmpty() && (currentTime - timestamps.peek() > TIME_WINDOW_MS)) {
            timestamps.poll();
        }

        if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
            return false;
        }

        timestamps.offer(currentTime);
        return true;
    }

    public boolean isAllowed(String clientIp, String action) {
        return tryAcquire(clientIp + ":" + action);
    }
}
