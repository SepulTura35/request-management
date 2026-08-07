package com.enoca.requestmanagement.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Giriş deneme sınırı")
class LoginAttemptServiceTest {

    private static final String EMAIL = "deneme@enoca.com";

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
    }

    @Test
    @DisplayName("Eşiğin altındaki denemeler engellenmez")
    void staysOpenBelowThreshold() {
        LoginAttemptService service = new LoginAttemptService(Clock.systemUTC());

        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL);
        }

        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("Beşinci başarısız denemeden sonra engellenir")
    void blocksAtThreshold() {
        LoginAttemptService service = new LoginAttemptService(Clock.systemUTC());

        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL);
        }

        assertThat(service.isBlocked(EMAIL)).isTrue();
    }

    @Test
    @DisplayName("Başarılı giriş sayacı sıfırlar")
    void resetClearsTheCounter() {
        LoginAttemptService service = new LoginAttemptService(Clock.systemUTC());
        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL);
        }

        service.reset(EMAIL);

        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("Engel süresi dolunca tekrar giriş denenebilir")
    void blockExpiresAfterTheWindow() {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-07T10:00:00Z"));
        LoginAttemptService service = new LoginAttemptService(clock);
        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL);
        }
        assertThat(service.isBlocked(EMAIL)).isTrue();

        clock.advance(Duration.ofMinutes(16));

        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    @Test
    @DisplayName("Farklı e-postalar birbirini etkilemez")
    void countersAreIsolatedPerEmail() {
        LoginAttemptService service = new LoginAttemptService(Clock.systemUTC());

        for (int i = 0; i < 5; i++) {
            service.recordFailure("saldirgan@enoca.com");
        }

        assertThat(service.isBlocked("masum@enoca.com")).isFalse();
    }

    @Test
    @DisplayName("E-posta büyük/küçük harf ve boşluktan bağımsız eşleşir")
    void keyIsNormalised() {
        LoginAttemptService service = new LoginAttemptService(Clock.systemUTC());

        for (int i = 0; i < 5; i++) {
            service.recordFailure("  Deneme@Enoca.com ");
        }

        assertThat(service.isBlocked(EMAIL)).isTrue();
    }
}
