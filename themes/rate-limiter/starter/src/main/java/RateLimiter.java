/**
 * See README.md for the full spec. Implement section by section — don't change these
 * method signatures.
 */
public class RateLimiter {

    public RateLimiter(int defaultLimit, long defaultWindowMs) {
        // TODO (Section 1): store the default config and whatever state you need
        // (per-client window counters, later: overrides, sliding logs, block list, totals).
        throw new UnsupportedOperationException("Not implemented: constructor");
    }

    // ── Section 1 ──────────────────────────────────────────────────────────

    public boolean allowRequest(String clientId, long timestamp) {
        // TODO (Section 1): fixed-window counter. See README for exact window/reset rules.
        throw new UnsupportedOperationException("Not implemented: allowRequest");
    }

    public int getRemaining(String clientId, long timestamp) {
        // TODO (Section 1): pure read, must not mutate state.
        throw new UnsupportedOperationException("Not implemented: getRemaining");
    }

    // ── Section 2 ──────────────────────────────────────────────────────────

    public boolean allowRequestSliding(String clientId, long timestamp) {
        // TODO (Section 2): sliding window log, independent state from allowRequest.
        throw new UnsupportedOperationException("Not implemented: allowRequestSliding");
    }

    // ── Section 3 ──────────────────────────────────────────────────────────

    public void setClientLimit(String clientId, int limit, long windowMs) {
        // TODO (Section 3)
        throw new UnsupportedOperationException("Not implemented: setClientLimit");
    }

    public void blockClient(String clientId) {
        // TODO (Section 3)
        throw new UnsupportedOperationException("Not implemented: blockClient");
    }

    public void unblockClient(String clientId) {
        // TODO (Section 3)
        throw new UnsupportedOperationException("Not implemented: unblockClient");
    }

    public boolean isBlocked(String clientId) {
        // TODO (Section 3)
        throw new UnsupportedOperationException("Not implemented: isBlocked");
    }

    // ── Section 4 ──────────────────────────────────────────────────────────

    public int getTotalAllowed(String clientId) {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getTotalAllowed");
    }

    public int getTotalRejected(String clientId) {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getTotalRejected");
    }

    public String getMostActiveClient() {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getMostActiveClient");
    }
}
