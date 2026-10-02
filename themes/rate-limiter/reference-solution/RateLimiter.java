// Reference solution — only look at this after finishing (or when truly stuck).
// Drop this in place of src/main/java/RateLimiter.java to compare.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RateLimiter {

    private record ClientConfig(int limit, long windowMs) {
    }

    private static class FixedWindowState {
        long windowIndex;
        int count;

        FixedWindowState(long windowIndex, int count) {
            this.windowIndex = windowIndex;
            this.count = count;
        }
    }

    private static class Totals {
        int allowed;
        int rejected;
    }

    private final ClientConfig defaultConfig;
    private final Map<String, ClientConfig> overrides = new HashMap<>();
    private final Map<String, FixedWindowState> fixedState = new HashMap<>();
    private final Map<String, List<Long>> slidingLog = new HashMap<>();
    private final Set<String> blocked = new HashSet<>();
    private final Map<String, Totals> totals = new HashMap<>();

    public RateLimiter(int defaultLimit, long defaultWindowMs) {
        this.defaultConfig = new ClientConfig(defaultLimit, defaultWindowMs);
    }

    // ── Section 1 ──────────────────────────────────────────────────────────

    public boolean allowRequest(String clientId, long timestamp) {
        if (blocked.contains(clientId)) {
            recordTotal(clientId, false);
            return false;
        }

        ClientConfig config = resolveConfig(clientId);
        long windowIndex = Math.floorDiv(timestamp, config.windowMs());

        FixedWindowState state = fixedState.get(clientId);
        if (state == null || state.windowIndex != windowIndex) {
            state = new FixedWindowState(windowIndex, 0);
            fixedState.put(clientId, state);
        }
        state.count++;

        boolean allowed = state.count <= config.limit();
        recordTotal(clientId, allowed);
        return allowed;
    }

    public int getRemaining(String clientId, long timestamp) {
        ClientConfig config = resolveConfig(clientId);
        long windowIndex = Math.floorDiv(timestamp, config.windowMs());
        FixedWindowState state = fixedState.get(clientId);
        int count = (state != null && state.windowIndex == windowIndex) ? state.count : 0;
        return Math.max(0, config.limit() - count);
    }

    // ── Section 2 ──────────────────────────────────────────────────────────

    public boolean allowRequestSliding(String clientId, long timestamp) {
        if (blocked.contains(clientId)) {
            recordTotal(clientId, false);
            return false;
        }

        ClientConfig config = resolveConfig(clientId);
        List<Long> log = new ArrayList<>(slidingLog.getOrDefault(clientId, List.of()));
        log.removeIf(t -> t <= timestamp - config.windowMs());

        boolean allowed = log.size() < config.limit();
        if (allowed) log.add(timestamp);
        slidingLog.put(clientId, log);

        recordTotal(clientId, allowed);
        return allowed;
    }

    // ── Section 3 ──────────────────────────────────────────────────────────

    public void setClientLimit(String clientId, int limit, long windowMs) {
        overrides.put(clientId, new ClientConfig(limit, windowMs));
    }

    public void blockClient(String clientId) {
        blocked.add(clientId);
    }

    public void unblockClient(String clientId) {
        blocked.remove(clientId);
    }

    public boolean isBlocked(String clientId) {
        return blocked.contains(clientId);
    }

    // ── Section 4 ──────────────────────────────────────────────────────────

    public int getTotalAllowed(String clientId) {
        Totals t = totals.get(clientId);
        return t == null ? 0 : t.allowed;
    }

    public int getTotalRejected(String clientId) {
        Totals t = totals.get(clientId);
        return t == null ? 0 : t.rejected;
    }

    public String getMostActiveClient() {
        String best = null;
        int bestTotal = -1;
        for (Map.Entry<String, Totals> entry : totals.entrySet()) {
            int sum = entry.getValue().allowed + entry.getValue().rejected;
            if (sum > bestTotal) {
                bestTotal = sum;
                best = entry.getKey();
            }
        }
        return best;
    }

    private ClientConfig resolveConfig(String clientId) {
        return overrides.getOrDefault(clientId, defaultConfig);
    }

    private void recordTotal(String clientId, boolean allowed) {
        Totals t = totals.computeIfAbsent(clientId, id -> new Totals());
        if (allowed) t.allowed++;
        else t.rejected++;
    }
}
