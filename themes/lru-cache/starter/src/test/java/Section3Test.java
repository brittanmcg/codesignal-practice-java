import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 3: explicit removal and recency introspection")
class Section3Test {

    @Test
    @DisplayName("getKeysByRecency lists most-recently-used first")
    void getKeysByRecencyListsMostRecentFirst() {
        Cache<Integer> cache = new Cache<>(3);
        cache.set("a", 1, 0);
        cache.set("b", 2, 0);
        cache.set("c", 3, 0);
        assertEquals(List.of("c", "b", "a"), cache.getKeysByRecency());

        cache.get("a", 0); // 'a' becomes most-recently-used
        assertEquals(List.of("a", "c", "b"), cache.getKeysByRecency());
    }

    @Test
    @DisplayName("delete removes a key and reports whether it was present")
    void deleteRemovesAndReports() {
        Cache<Integer> cache = new Cache<>(3);
        cache.set("a", 1, 0);
        cache.set("b", 2, 0);

        assertTrue(cache.delete("a"));
        assertFalse(cache.has("a", 0));
        assertEquals(1, cache.size());
        assertFalse(cache.delete("a"));
        assertFalse(cache.delete("never-existed"));
    }

    @Test
    @DisplayName("clear empties the cache entirely")
    void clearEmptiesCache() {
        Cache<Integer> cache = new Cache<>(3);
        cache.set("a", 1, 0);
        cache.set("b", 2, 0);

        cache.clear();

        assertEquals(0, cache.size());
        assertFalse(cache.has("a", 0));
        assertEquals(List.of(), cache.getKeysByRecency());
    }
}
