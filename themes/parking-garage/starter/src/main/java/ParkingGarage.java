import java.util.List;
import java.util.Map;

/**
 * See README.md for the full spec. Implement section by section — don't change these
 * method signatures or the record types in the other files.
 */
public class ParkingGarage {

    public ParkingGarage(List<SpotConfig> spots) {
        // TODO (Section 1): store the spot configuration and any state you need
        // to track which spots are occupied.
        throw new UnsupportedOperationException("Not implemented: constructor");
    }

    // ── Section 1 ──────────────────────────────────────────────────────────

    public Ticket park(Vehicle vehicle, long entryTime) {
        // TODO (Section 1): assign a free, size-compatible spot and return a Ticket.
        // Return null if the plate is already parked, or no spot fits (Section 3
        // will change "no spot fits" into a waitlist addition instead).
        throw new UnsupportedOperationException("Not implemented: park");
    }

    public ExitReceipt unpark(String licensePlate, long exitTime) {
        // TODO (Section 1): free the vehicle's spot(s) and return a receipt with
        // the correct fee (Section 2 adds the real billing rules).
        // Return null if the plate isn't currently parked.
        throw new UnsupportedOperationException("Not implemented: unpark");
    }

    public int availableSpotsCount() {
        // TODO (Section 1)
        throw new UnsupportedOperationException("Not implemented: availableSpotsCount");
    }

    public boolean isParked(String licensePlate) {
        // TODO (Section 1)
        throw new UnsupportedOperationException("Not implemented: isParked");
    }

    // ── Section 3 ──────────────────────────────────────────────────────────

    public List<String> getWaitlist() {
        // TODO (Section 3): plates currently waiting, FIFO order.
        throw new UnsupportedOperationException("Not implemented: getWaitlist");
    }

    public boolean isWaitlisted(String licensePlate) {
        // TODO (Section 3)
        throw new UnsupportedOperationException("Not implemented: isWaitlisted");
    }

    // ── Section 4 ──────────────────────────────────────────────────────────

    public double getTotalRevenue() {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getTotalRevenue");
    }

    public Map<VehicleType, Double> getRevenueByVehicleType() {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getRevenueByVehicleType");
    }

    public double getAverageDurationMs() {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getAverageDurationMs");
    }

    public VehicleType getBusiestVehicleType() {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getBusiestVehicleType");
    }

    public int getPeakVehicleCount() {
        // TODO (Section 4)
        throw new UnsupportedOperationException("Not implemented: getPeakVehicleCount");
    }
}
