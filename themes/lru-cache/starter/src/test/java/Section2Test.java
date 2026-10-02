import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 2: TTL expiration")
class Section2Test {

    @Test
    @DisplayName("an entry with a ttl expires and get() removes it")
    void entryWithTtlExpires() {
        Cache<Integer> cache = new Cache<>(5);
        cache.set("a", 1, 0, 1000); // expires at t=1000

        assertEquals(1, cache.get("a", 500));
        assertNull(cache.get("a", 1000)); // expires exactly at its deadline
    }

    @Test
    @DisplayName("has() respects expiration without needing a prior get() to clean up")
    void hasRespectsExpiration() {
        Cache<Integer> cache = new Cache<>(5);
        cache.set("b", 2, 0, 1000);

        assertTrue(cache.has("b", 999));
        assertFalse(cache.has("b", 1000));
    }

    @Test
    @DisplayName("an entry without a ttl never expires")
    void entryWithoutTtlNeverExpires() {
        Cache<Integer> cache = new Cache<>(5);
        cache.set("c", 3, 0);

        assertEquals(3, cache.get("c", 10_000_000));
    }

    @Test
    @DisplayName("updating an existing key replaces its ttl entirely")
    void updatingKeyReplacesTtl() {
        Cache<Integer> cache = new Cache<>(5);
        cache.set("d", 1, 0, 1000); // would expire at 1000
        cache.set("d", 2, 500, 2000); // now expires at 2500

        assertEquals(2, cache.get("d", 2400));
        assertNull(cache.get("d", 2500));
    }
}
