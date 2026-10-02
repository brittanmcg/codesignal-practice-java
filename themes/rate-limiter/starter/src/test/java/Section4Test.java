import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 4: analytics and edge cases")
class Section4Test {

    @Test
    @DisplayName("getTotalAllowed/getTotalRejected accumulate across both algorithms")
    void totalsAccumulateAcrossAlgorithms() {
        RateLimiter limiter = new RateLimiter(2, 1000);

        assertTrue(limiter.allowRequest("X", 0));
        assertTrue(limiter.allowRequest("X", 0));
        assertFalse(limiter.allowRequest("X", 0));
        assertTrue(limiter.allowRequestSliding("X", 0));

        assertEquals(3, limiter.getTotalAllowed("X"));
        assertEquals(1, limiter.getTotalRejected("X"));
    }

    @Test
    @DisplayName("getMostActiveClient returns the busiest client, null when nobody has made a request")
    void mostActiveClientReturnsBusiest() {
        RateLimiter empty = new RateLimiter(2, 1000);
        assertNull(empty.getMostActiveClient());

        RateLimiter limiter = new RateLimiter(2, 1000);
        limiter.allowRequest("X", 0);
        limiter.allowRequest("X", 0);
        limiter.allowRequest("X", 0); // rejected, still counts
        limiter.allowRequestSliding("X", 0);
        limiter.allowRequest("Y", 0);

        assertEquals("X", limiter.getMostActiveClient());
    }

    @Test
    @DisplayName("blocked-and-rejected calls count toward rejected but never allowed, even after unblocking")
    void blockedRejectedCallsCountCorrectly() {
        RateLimiter limiter = new RateLimiter(2, 1000);
        limiter.blockClient("Z");
        limiter.allowRequest("Z", 0);

        assertEquals(1, limiter.getTotalRejected("Z"));
        assertEquals(0, limiter.getTotalAllowed("Z"));

        limiter.unblockClient("Z");
        limiter.allowRequest("Z", 0);

        assertEquals(1, limiter.getTotalAllowed("Z"));
        assertEquals(1, limiter.getTotalRejected("Z"));
    }
}
