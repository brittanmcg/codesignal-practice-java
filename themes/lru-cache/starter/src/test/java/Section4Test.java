import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 4: analytics and edge cases")
class Section4Test {

    @Test
    @DisplayName("getHitRate and getEvictionCount track hits/misses and capacity evictions separately")
    void hitRateAndEvictionCountTrackedSeparately() {
        Cache<Integer> cache = new Cache<>(2);
        cache.set("a", 1, 0);
        cache.set("b", 2, 0);
        cache.get("a", 0); // hit, 'a' becomes MRU
        cache.get("z", 0); // miss
        cache.set("c", 3, 0); // evicts 'b' (LRU)
        cache.get("b", 0); // miss ('b' was evicted)

        assertEquals(1.0 / 3, cache.getHitRate(), 0.0001);
        assertEquals(1, cache.getEvictionCount());
    }

    @Test
    @DisplayName("getExpiredCount is tracked separately from capacity evictions")
    void expiredCountTrackedSeparately() {
        Cache<Integer> cache = new Cache<>(5);
        cache.set("d", 4, 0, 100);
        cache.get("d", 50); // hit
        cache.get("d", 150); // expired -> counts as expired AND a miss

        assertEquals(1, cache.getExpiredCount());
        assertEquals(0, cache.getEvictionCount());
    }

    @Test
    @DisplayName("getMostAccessedKey tracks get() calls by key, including on unknown keys")
    void mostAccessedKeyTracksByKey() {
        Cache<Integer> cache = new Cache<>(5);
        cache.set("a", 1, 0);
        cache.set("b", 2, 0);
        cache.get("a", 0);
        cache.get("a", 0);
        cache.get("b", 0);
        cache.get("z", 0);
        cache.get("a", 0);

        assertEquals("a", cache.getMostAccessedKey());
    }

    @Test
    @DisplayName("getMostAccessedKey is null and getHitRate is 0 before any get() call")
    void nullAndZeroBeforeAnyGet() {
        Cache<Integer> cache = new Cache<>(2);
        assertNull(cache.getMostAccessedKey());
        assertEquals(0.0, cache.getHitRate(), 0.0001);
    }
}
