// Reference solution — only look at this after finishing (or when truly stuck).
// Drop this in place of src/main/java/Cache.java to compare.

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Cache<V> {

    private static class Entry<V> {
        final V value;
        final Long expiresAt; // null = never expires

        Entry(V value, Long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }

        boolean isExpired(long now) {
            return expiresAt != null && now >= expiresAt;
        }
    }

    private final int capacity;
    // Insertion order doubles as recency order: first = least recently used, last = most.
    private final LinkedHashMap<String, Entry<V>> store = new LinkedHashMap<>();
    private final Map<String, Integer> accessCount = new LinkedHashMap<>();
    private int hits = 0;
    private int misses = 0;
    private int evictions = 0;
    private int expiredCount = 0;

    public Cache(int capacity) {
        this.capacity = capacity;
    }

    // ── Section 1 & 2 ──────────────────────────────────────────────────────

    public void set(String key, V value, long now) {
        put(key, value, null);
    }

    public void set(String key, V value, long now, long ttlMs) {
        put(key, value, now + ttlMs);
    }

    private void put(String key, V value, Long expiresAt) {
        if (store.containsKey(key)) {
            store.remove(key);
        } else if (store.size() >= capacity) {
            Iterator<String> it = store.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
                evictions++;
            }
        }
        store.put(key, new Entry<>(value, expiresAt));
    }

    public V get(String key, long now) {
        accessCount.merge(key, 1, Integer::sum);

        Entry<V> entry = store.get(key);
        if (entry == null) {
            misses++;
            return null;
        }

        if (entry.isExpired(now)) {
            store.remove(key);
            expiredCount++;
            misses++;
            return null;
        }

        store.remove(key);
        store.put(key, entry);
        hits++;
        return entry.value;
    }

    public boolean has(String key, long now) {
        Entry<V> entry = store.get(key);
        return entry != null && !entry.isExpired(now);
    }

    public int size() {
        return store.size();
    }

    // ── Section 3 ──────────────────────────────────────────────────────────

    public boolean delete(String key) {
        return store.remove(key) != null;
    }

    public void clear() {
        store.clear();
    }

    public List<String> getKeysByRecency() {
        List<String> keys = new ArrayList<>(store.keySet());
        Collections.reverse(keys);
        return keys;
    }

    // ── Section 4 ──────────────────────────────────────────────────────────

    public double getHitRate() {
        int total = hits + misses;
        return total == 0 ? 0 : (double) hits / total;
    }

    public int getEvictionCount() {
        return evictions;
    }

    public int getExpiredCount() {
        return expiredCount;
    }

    public String getMostAccessedKey() {
        String best = null;
        int bestCount = -1;
        for (Map.Entry<String, Integer> e : accessCount.entrySet()) {
            if (e.getValue() > bestCount) {
                bestCount = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }
}
