package com.bloggingapp.backend.service;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AiRateLimiter {

    private final long maxRequests;
    private final long windowMillis;
    private final Clock clock;
    private final ConcurrentMap<String, Window> windows = new ConcurrentHashMap<>();

    @Autowired
    public AiRateLimiter(
            @Value("${ai.rate-limit.max-requests:10}") long maxRequests,
            @Value("${ai.rate-limit.window-ms:60000}") long windowMillis) {
        this(maxRequests, windowMillis, Clock.systemUTC());
    }

    AiRateLimiter(long maxRequests, long windowMillis, Clock clock) {
        if (maxRequests < 1 || windowMillis < 1) {
            throw new IllegalArgumentException("AI rate-limit settings must be positive");
        }
        this.maxRequests = maxRequests;
        this.windowMillis = windowMillis;
        this.clock = clock;
    }

    public boolean allow(String key) {
        long now = clock.millis();
        Window updated = windows.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAt >= windowMillis) {
                return new Window(now, 1);
            }
            if (current.requests >= maxRequests) {
                return new Window(current.startedAt, maxRequests + 1);
            }
            return new Window(current.startedAt, current.requests + 1);
        });
        return updated.requests <= maxRequests && now - updated.startedAt < windowMillis;
    }

    private record Window(long startedAt, long requests) {
    }
}
