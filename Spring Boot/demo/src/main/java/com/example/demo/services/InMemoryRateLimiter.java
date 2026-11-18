package com.example.demo.services;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import com.example.demo.exception.ApiException;
import org.springframework.stereotype.Service;

@Service
public class InMemoryRateLimiter {
    private record Counter(long expiresAt, int requests) { }
    private final ConcurrentHashMap<String, Counter> windows = new ConcurrentHashMap<>();
    private final AtomicLong requestCount = new AtomicLong();

    public void enforce(String category, Long userId, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long expiry = now + Math.max(1, window.toMillis());
        AtomicBoolean rejected = new AtomicBoolean();
        windows.compute(category + ":" + userId, (key, counter) -> {
            if (counter == null || counter.expiresAt() <= now) return new Counter(expiry, 1);
            int requests = counter.requests() + 1;
            if (requests > maxRequests) rejected.set(true);
            return new Counter(counter.expiresAt(), requests);
        });
        if (requestCount.incrementAndGet() % 1024 == 0) {
            windows.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= now);
        }
        if (rejected.get()) throw ApiException.tooManyRequests("Too many requests. Please try again later.");
    }
}
