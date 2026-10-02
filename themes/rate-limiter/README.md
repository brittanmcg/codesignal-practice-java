# Assessment: API Rate Limiter

**Estimated total time:** 70–90 minutes across 4 sections.

## How to work this assessment

- Work in order; don't read a later section until the current one's tests pass.
- Implement everything in `src/main/java/RateLimiter.java`. Method signatures are already declared —
  fill in the bodies. Don't change signatures.
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

A rate limiter tracks requests per `clientId` and decides whether to allow or reject each one. All
methods take an explicit `timestamp`/`now` as a `long` in milliseconds (never read the real clock) so
behavior is fully deterministic and testable.

---

## Section 1 — Fixed window limiting (easy)

The constructor takes a **default** limit and window size that apply to every client unless
overridden (Section 3 adds per-client overrides — don't design around them yet, just don't paint
yourself into a corner where they'd be impossible to add).

Implement:
- `allowRequest(clientId, timestamp)` — a fixed-window counter. Divide time into windows of
  `windowMs` starting at epoch 0 (`windowIndex = floor(timestamp / windowMs)`). Each call
  increments that client's counter for the current window (even if the call is ultimately rejected)
  and returns `true` if the count is within the limit, `false` otherwise. When `timestamp` falls into
  a new window index than the client's last recorded one, the counter for that client resets to
  start counting the new window.
- `getRemaining(clientId, timestamp)` — how many more requests the client could make in the window
  containing `timestamp` (`limit - count`, clamped to `0`, and using the *current* window's count —
  a fresh window's remaining count is the full limit). This is a pure read: calling it must not
  itself count as a request or otherwise mutate state.

## Section 2 — Sliding window log (medium)

Fixed windows have a flaw: a client can burst up to `2x` the limit right at a window boundary. Add a
more accurate algorithm as a **separate** method (don't touch `allowRequest` — both algorithms need
to keep working, independently, for the rest of the assessment):

- `allowRequestSliding(clientId, timestamp)` — keeps a log of the exact timestamps of a client's
  *allowed* requests (rejected requests don't enter the log). On each call, first drop any logged
  timestamps that are no longer within `windowMs` of `timestamp` (i.e., keep only those `>
  timestamp - windowMs`), then allow the request (log it and return `true`) if the remaining log is
  shorter than the limit, otherwise reject it (`false`, log unchanged). This method should resolve
  the same limit/window a client would get from `allowRequest` (default, or that client's override
  once Section 3 adds overrides) — it's a different algorithm over the same configured budget, not a
  different budget.

## Section 3 — Per-client limits and blocking (medium-hard)

Implement:
- `setClientLimit(clientId, limit, windowMs)` — from now on, this client uses this limit/window
  instead of the default, for **both** `allowRequest` and `allowRequestSliding`. It does not
  retroactively change anything already counted.
- `blockClient(clientId)` / `unblockClient(clientId)` / `isBlocked(clientId)` — a blocked client's
  calls to `allowRequest` and `allowRequestSliding` are always rejected, regardless of their window
  state, and being blocked doesn't reset or otherwise disturb their window/log state. Unblocking
  lets normal window logic resume exactly where it would have been.

## Section 4 — Analytics and edge cases (hard)

Implement, tracked across a client's entire history (not just the current window):
- `getTotalAllowed(clientId)` — count of calls (to either `allowRequest` or `allowRequestSliding`)
  that returned `true`, ever.
- `getTotalRejected(clientId)` — count of calls that returned `false`, ever — including ones
  rejected because the client was blocked.
- `getMostActiveClient()` — the `clientId` with the highest `totalAllowed + totalRejected`, or
  `null` if no client has made any request yet.

Watch the edge cases: window rollover must actually reset the count (not just cap it), a per-client
override must not affect other clients or retroactively rewrite history, and blocked-and-rejected
calls must still count toward `getTotalRejected` (but never `getTotalAllowed`) even after the client
is later unblocked.
