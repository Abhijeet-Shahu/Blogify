package com.bloggingapp.backend.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class AiRateLimiterTest {

    @Test
    void rejectsRequestsAfterConfiguredLimit() {
        AiRateLimiter limiter = new AiRateLimiter(2, 1_000, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        assertTrue(limiter.allow("client"));
        assertTrue(limiter.allow("client"));
        assertFalse(limiter.allow("client"));
    }

    @Test
    void tracksClientsIndependently() {
        AiRateLimiter limiter = new AiRateLimiter(1, 1_000, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        assertTrue(limiter.allow("first"));
        assertFalse(limiter.allow("first"));
        assertTrue(limiter.allow("second"));
    }
}
