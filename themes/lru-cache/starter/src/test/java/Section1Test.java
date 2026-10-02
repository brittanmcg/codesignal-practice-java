import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 1: capacity and LRU eviction")
class Section1Test {

    @Test
    @DisplayName("evicts the least-recently-used entry when capacity is exceeded")
    void evictsLeastRecentlyUsed() {
        Cache<Integer> cache = new Cache<>(2);
        cache.set("a", 1, 0);
        cache.set("b", 2, 0);
        assertEquals(1, cache.get("a", 0)); // 'a' is now most-recently-used

        cache.set("c", 3, 0); // 'b' is LRU, gets evicted

        assertNull(cache.get("b", 0));
        assertTrue(cache.has("a", 0));
        assertTrue(cache.has("c", 0));
        assertEquals(2, cache.size());
    }

    @Test
    @DisplayName("has() does not affect recency order")
    void hasDoesNotAffectRecency() {
        Cache<Integer> cache = new Cache<>(2);
        cache.set("a", 1, 0);
        cache.set("b", 2, 0);
        cache.has("a", 0); // peek only, 'a' should still be LRU

        cache.set("c", 3, 0); // 'a' is still LRU, gets evicted (not 'b')

        assertNull(cache.get("a", 0));
        assertEquals(2, cache.get("b", 0));
        assertTrue(cache.has("c", 0));
    }

    @Test
    @DisplayName("works with capacity 1")
    void worksWithCapacityOne() {
        Cache<Integer> cache = new Cache<>(1);
        cache.set("x", 1, 0);
        cache.set("y", 2, 0);

        assertNull(cache.get("x", 0));
        assertEquals(2, cache.get("y", 0));
        assertEquals(1, cache.size());
    }

    @Test
    @DisplayName("get() on a missing key returns null without affecting size")
    void getOnMissingKeyReturnsNull() {
        Cache<Integer> cache = new Cache<>(3);
        cache.set("a", 1, 0);

        assertNull(cache.get("nope", 0));
        assertEquals(1, cache.size());
    }
}
