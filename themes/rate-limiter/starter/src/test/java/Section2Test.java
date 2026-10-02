import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 2: sliding window log")
class Section2Test {

    @Test
    @DisplayName("allows up to the limit within the trailing window, independent of fixed windows")
    void allowsUpToLimitWithinTrailingWindow() {
        RateLimiter limiter = new RateLimiter(3, 10000);

        assertTrue(limiter.allowRequestSliding("A", 0));
        assertTrue(limiter.allowRequestSliding("A", 2000));
        assertTrue(limiter.allowRequestSliding("A", 4000));
        assertFalse(limiter.allowRequestSliding("A", 5000)); // all 3 prior are still within 10s
    }

    @Test
    @DisplayName("old entries fall out of the trailing window and free up budget")
    void oldEntriesFallOutOfWindow() {
        RateLimiter limiter = new RateLimiter(3, 10000);
        limiter.allowRequestSliding("A", 0);
        limiter.allowRequestSliding("A", 2000);
        limiter.allowRequestSliding("A", 4000);
        limiter.allowRequestSliding("A", 5000); // rejected, doesn't enter the log

        // at t=10001, the log's window is (1, 10001] — the t=0 entry has aged out
        assertTrue(limiter.allowRequestSliding("A", 10001));
    }

    @Test
    @DisplayName("the fixed-window and sliding-window algorithms keep independent state")
    void fixedAndSlidingKeepIndependentState() {
        RateLimiter limiter = new RateLimiter(1, 60000);

        assertTrue(limiter.allowRequest("A", 0));
        assertFalse(limiter.allowRequest("A", 0)); // fixed window exhausted

        assertTrue(limiter.allowRequestSliding("A", 0)); // sliding log starts fresh
    }
}
