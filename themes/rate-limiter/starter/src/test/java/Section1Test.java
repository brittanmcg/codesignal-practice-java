import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 1: fixed window limiting")
class Section1Test {

    @Test
    @DisplayName("allows requests up to the limit within a window, then rejects")
    void allowsUpToLimitThenRejects() {
        RateLimiter limiter = new RateLimiter(3, 60000);

        assertTrue(limiter.allowRequest("A", 0));
        assertTrue(limiter.allowRequest("A", 1000));
        assertTrue(limiter.allowRequest("A", 2000));
        assertFalse(limiter.allowRequest("A", 3000));
    }

    @Test
    @DisplayName("getRemaining reflects the current window without mutating state")
    void getRemainingDoesNotMutateState() {
        RateLimiter limiter = new RateLimiter(3, 60000);
        limiter.allowRequest("A", 0);

        assertEquals(2, limiter.getRemaining("A", 100));
        assertEquals(2, limiter.getRemaining("A", 100)); // calling it again changes nothing
        assertEquals(3, limiter.getRemaining("FRESH", 0)); // never seen this client: full limit
    }

    @Test
    @DisplayName("a new window resets the counter")
    void newWindowResetsCounter() {
        RateLimiter limiter = new RateLimiter(3, 60000);
        limiter.allowRequest("A", 0);
        limiter.allowRequest("A", 1000);
        limiter.allowRequest("A", 2000);
        assertFalse(limiter.allowRequest("A", 3000));

        assertTrue(limiter.allowRequest("A", 60000)); // windowIndex 1 now
        assertEquals(2, limiter.getRemaining("A", 60000));
    }

    @Test
    @DisplayName("clients are tracked independently")
    void clientsTrackedIndependently() {
        RateLimiter limiter = new RateLimiter(1, 60000);

        assertTrue(limiter.allowRequest("A", 0));
        assertFalse(limiter.allowRequest("A", 0));
        assertTrue(limiter.allowRequest("B", 0));
    }
}
