package com.enoca.requestmanagement.security;

import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final Clock clock;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService() {
        this(Clock.systemUTC());
    }

    LoginAttemptService(Clock clock) {
        this.clock = clock;
    }

    public boolean isBlocked(String email) {
        Attempt attempt = attempts.get(key(email));
        return attempt != null
                && attempt.blockedUntil != null
                && Instant.now(clock).isBefore(attempt.blockedUntil);
    }

    public void recordFailure(String email) {
        Instant now = Instant.now(clock);
        attempts.compute(key(email), (ignored, existing) -> {
            boolean windowExpired = existing != null
                    && existing.blockedUntil != null
                    && !now.isBefore(existing.blockedUntil);

            int count = (existing == null || windowExpired) ? 1 : existing.count + 1;
            Instant blockedUntil = count >= MAX_ATTEMPTS ? now.plus(BLOCK_DURATION) : null;
            return new Attempt(count, blockedUntil);
        });
    }

    public void reset(String email) {
        attempts.remove(key(email));
    }

    private String key(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static final class Attempt {
        private final int count;
        private final Instant blockedUntil;

        private Attempt(int count, Instant blockedUntil) {
            this.count = count;
            this.blockedUntil = blockedUntil;
        }
    }
}
