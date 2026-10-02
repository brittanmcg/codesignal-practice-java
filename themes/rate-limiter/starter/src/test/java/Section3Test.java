import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 3: per-client limits and blocking")
class Section3Test {

    @Test
    @DisplayName("a per-client override changes that client only, not the default for others")
    void perClientOverrideChangesOnlyThatClient() {
        RateLimiter limiter = new RateLimiter(3, 60000);
        limiter.setClientLimit("VIP", 10, 60000);

        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.allowRequest("VIP", i * 100L));
        }
        assertFalse(limiter.allowRequest("VIP", 1000));

        assertTrue(limiter.allowRequest("REG", 0));
        assertTrue(limiter.allowRequest("REG", 0));
        assertTrue(limiter.allowRequest("REG", 0));
        assertFalse(limiter.allowRequest("REG", 0));
    }

    @Test
    @DisplayName("a blocked client is always rejected on both algorithms, regardless of window state")
    void blockedClientAlwaysRejected() {
        RateLimiter limiter = new RateLimiter(3, 60000);
        limiter.blockClient("BAD");

        assertTrue(limiter.isBlocked("BAD"));
        assertFalse(limiter.allowRequest("BAD", 0));
        assertFalse(limiter.allowRequestSliding("BAD", 0));
    }

    @Test
    @DisplayName("unblocking resumes normal window logic without leftover state from being blocked")
    void unblockingResumesNormalLogic() {
        RateLimiter limiter = new RateLimiter(3, 60000);
        limiter.blockClient("BAD");
        limiter.allowRequest("BAD", 0);
        limiter.allowRequest("BAD", 0);
        limiter.unblockClient("BAD");

        assertFalse(limiter.isBlocked("BAD"));
        assertTrue(limiter.allowRequest("BAD", 0));
        assertTrue(limiter.allowRequest("BAD", 0));
        assertTrue(limiter.allowRequest("BAD", 0));
        assertFalse(limiter.allowRequest("BAD", 0)); // 4th real request, limit 3
    }
}
