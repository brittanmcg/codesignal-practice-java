# Assessment: LRU Cache with TTL

**Estimated total time:** 70–90 minutes across 4 sections.

## How to work this assessment

- Work in order; don't read a later section until the current one's tests pass.
- Implement everything in `src/main/java/Cache.java`. Method signatures are already declared —
  including a `set` overload that takes a `ttlMs` that Section 1 doesn't use yet (that's intentional,
  matches how these signatures work in the real thing — don't change them).
- Run only the section you're on, from inside your attempt's directory:

  ```bash
  cd attempts/<your-attempt-dir>
  mvn test -Dtest=Section1Test
  ```

  All four test classes should pass together by the end (`mvn test`) — later sections deepen the same
  class.
- Don't look at `reference-solution/` until you're done or stuck.

---

## Domain model

A fixed-capacity cache of `String` keys to values of type `V` (`Cache<V>`). All time-based methods
take an explicit `now` as a `long` in milliseconds (never read the real clock), so behavior is fully
deterministic and testable. Where a key is absent, `get` returns `null`.

---

## Section 1 — Capacity and LRU eviction (easy)

Implement:
- `set(key, value, now)` — inserts or updates a key. If inserting a **new** key would push the cache
  beyond `capacity`, evict the **least-recently-used** entry first. This overload never expires.
- `get(key, now)` — returns the value, or `null` if the key isn't present. A successful `get` marks
  that key as the **most-recently-used**.
- `has(key, now)` — checks presence **without** affecting recency order (a pure peek).
- `size()` — current number of entries.

"Most recently used" means: the most recently `set` or successfully `get`-ed. `has()` doesn't count.

## Section 2 — Time-to-live expiration (medium)

Give the `set(key, value, now, ttlMs)` overload real meaning:
- The entry expires at `now + ttlMs`. The 3-argument `set` never expires.
- `get(key, now)` must treat an expired entry as absent: return `null`, and actually remove it from
  the cache (don't leave dead weight taking up capacity).
- `has(key, now)` must also treat an expired entry as absent — but as a pure peek, it should
  determine this by checking the timestamp, not by relying on some earlier `get` call having already
  cleaned it up.
- Updating an existing key via either `set` overload replaces its TTL entirely (an update with the
  3-argument `set` makes it permanent, even if it previously had one).

## Section 3 — Explicit removal and recency introspection (medium-hard)

Implement:
- `delete(key)` — removes the key if present (`true`), or does nothing (`false`) if it wasn't there.
  This frees capacity but is a distinct thing from an automatic LRU eviction (Section 4 counts them
  separately).
- `clear()` — empties the cache entirely (`size()` becomes `0`, every key becomes absent).
- `getKeysByRecency()` — returns all keys ordered **most-recently-used first**. This is a pure peek:
  it must not itself change recency order.

## Section 4 — Analytics and edge cases (hard)

Implement, tracked across the cache's entire history:
- `getHitRate()` — of all `get()` calls ever made (not `has()`), the fraction that found a live,
  non-expired value. `0` if there have been no `get()` calls at all. A `get()` on an expired entry
  counts as a miss.
- `getEvictionCount()` — how many times `set()` has automatically evicted an LRU entry due to
  capacity. Explicit `delete()` calls never count here.
- `getExpiredCount()` — how many times `get()` has found and removed an expired entry.
- `getMostAccessedKey()` — the key string that has been passed to `get()` the most times, ever —
  `null` if `get()` has never been called. This tracks the key **string**, independent of whether
  that key is still in the cache (it might have been evicted, expired, or deleted long ago and still
  be the most-accessed key on record).

The trickiest part of this section is usually keeping `getEvictionCount` and `getExpiredCount`
cleanly separate from each other and from plain misses, while still having every expired removal also
count as a miss for `getHitRate`.
