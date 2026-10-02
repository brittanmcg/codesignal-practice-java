// Reference solution — only look at this after finishing (or when truly stuck).
// Drop this in place of src/main/java/ParkingGarage.java to compare.

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ParkingGarage {

    private static final Map<VehicleType, Double> RATE_PER_HOUR = new EnumMap<>(VehicleType.class);
    static {
        RATE_PER_HOUR.put(VehicleType.MOTORCYCLE, 2.0);
        RATE_PER_HOUR.put(VehicleType.CAR, 5.0);
        RATE_PER_HOUR.put(VehicleType.BUS, 15.0);
    }

    private static final double VIP_DISCOUNT = 0.2;
    private static final long HOUR_MS = 60 * 60 * 1000;

    private static class Spot {
        final String id;
        final SpotSize size;
        String occupiedBy;

        Spot(String id, SpotSize size) {
            this.id = id;
            this.size = size;
        }
    }

    private final List<Spot> spots;
    private final Map<String, Ticket> active = new LinkedHashMap<>();
    private final List<Vehicle> waitlist = new ArrayList<>();
    private final List<ExitReceipt> history = new ArrayList<>();
    private int currentVehicleCount = 0;
    private int peakVehicleCount = 0;

    public ParkingGarage(List<SpotConfig> spots) {
        this.spots = new ArrayList<>();
        for (SpotConfig s : spots) {
            this.spots.add(new Spot(s.id(), s.size()));
        }
    }

    public Ticket park(Vehicle vehicle, long entryTime) {
        if (active.containsKey(vehicle.licensePlate())) return null;

        List<String> spotIds = findSpotsFor(vehicle.type());
        if (spotIds == null) {
            waitlist.add(vehicle);
            return null;
        }

        return assign(vehicle, spotIds, entryTime);
    }

    public ExitReceipt unpark(String licensePlate, long exitTime) {
        Ticket ticket = active.get(licensePlate);
        if (ticket == null) return null;

        for (String id : ticket.spotIds()) {
            findSpot(id).occupiedBy = null;
        }
        active.remove(licensePlate);
        currentVehicleCount--;

        long hours = Math.max(1, ceilDiv(exitTime - ticket.entryTime(), HOUR_MS));
        double fee = RATE_PER_HOUR.get(ticket.vehicleType()) * hours;
        if (ticket.isVIP()) fee *= 1 - VIP_DISCOUNT;

        ExitReceipt receipt = new ExitReceipt(licensePlate, ticket.vehicleType(), fee, ticket.entryTime(), exitTime);
        history.add(receipt);

        autoParkFromWaitlist(exitTime);

        return receipt;
    }

    public int availableSpotsCount() {
        int count = 0;
        for (Spot s : spots) {
            if (s.occupiedBy == null) count++;
        }
        return count;
    }

    public boolean isParked(String licensePlate) {
        return active.containsKey(licensePlate);
    }

    public List<String> getWaitlist() {
        List<String> plates = new ArrayList<>();
        for (Vehicle v : waitlist) plates.add(v.licensePlate());
        return plates;
    }

    public boolean isWaitlisted(String licensePlate) {
        for (Vehicle v : waitlist) {
            if (v.licensePlate().equals(licensePlate)) return true;
        }
        return false;
    }

    public double getTotalRevenue() {
        double total = 0;
        for (ExitReceipt r : history) total += r.fee();
        return total;
    }

    public Map<VehicleType, Double> getRevenueByVehicleType() {
        Map<VehicleType, Double> result = new EnumMap<>(VehicleType.class);
        for (VehicleType t : VehicleType.values()) result.put(t, 0.0);
        for (ExitReceipt r : history) {
            result.put(r.vehicleType(), result.get(r.vehicleType()) + r.fee());
        }
        return result;
    }

    public double getAverageDurationMs() {
        if (history.isEmpty()) return 0;
        long total = 0;
        for (ExitReceipt r : history) total += (r.exitTime() - r.entryTime());
        return (double) total / history.size();
    }

    public VehicleType getBusiestVehicleType() {
        if (history.isEmpty()) return null;
        Map<VehicleType, Integer> counts = new EnumMap<>(VehicleType.class);
        for (VehicleType t : VehicleType.values()) counts.put(t, 0);
        for (ExitReceipt r : history) counts.put(r.vehicleType(), counts.get(r.vehicleType()) + 1);

        VehicleType best = null;
        int bestCount = -1;
        for (VehicleType t : VehicleType.values()) {
            if (counts.get(t) > bestCount) {
                bestCount = counts.get(t);
                best = t;
            }
        }
        return best;
    }

    public int getPeakVehicleCount() {
        return peakVehicleCount;
    }

    private Ticket assign(Vehicle vehicle, List<String> spotIds, long entryTime) {
        for (String id : spotIds) {
            findSpot(id).occupiedBy = vehicle.licensePlate();
        }

        Ticket ticket = new Ticket(vehicle.licensePlate(), vehicle.type(), spotIds, entryTime, vehicle.isVIP());
        active.put(vehicle.licensePlate(), ticket);

        currentVehicleCount++;
        if (currentVehicleCount > peakVehicleCount) {
            peakVehicleCount = currentVehicleCount;
        }

        return ticket;
    }

    private void autoParkFromWaitlist(long now) {
        boolean progressed = true;
        while (progressed) {
            progressed = false;
            for (int i = 0; i < waitlist.size(); i++) {
                Vehicle candidate = waitlist.get(i);
                List<String> spotIds = findSpotsFor(candidate.type());
                if (spotIds != null) {
                    waitlist.remove(i);
                    assign(candidate, spotIds, now);
                    progressed = true;
                    break;
                }
            }
        }
    }

    private List<String> findSpotsFor(VehicleType type) {
        if (type == VehicleType.MOTORCYCLE) {
            return findSingleSpot(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.LARGE);
        }
        if (type == VehicleType.CAR) {
            return findSingleSpot(SpotSize.MEDIUM, SpotSize.LARGE);
        }
        return findContiguousLargeSpots(3);
    }

    private List<String> findSingleSpot(SpotSize... sizesInPreferenceOrder) {
        for (SpotSize size : sizesInPreferenceOrder) {
            for (Spot s : spots) {
                if (s.size == size && s.occupiedBy == null) {
                    return List.of(s.id);
                }
            }
        }
        return null;
    }

    private List<String> findContiguousLargeSpots(int count) {
        List<Spot> run = new ArrayList<>();
        for (Spot s : spots) {
            if (s.size == SpotSize.LARGE && s.occupiedBy == null) {
                run.add(s);
                if (run.size() == count) {
                    List<String> ids = new ArrayList<>();
                    for (Spot spot : run) ids.add(spot.id);
                    return ids;
                }
            } else {
                run.clear();
            }
        }
        return null;
    }

    private Spot findSpot(String id) {
        for (Spot s : spots) {
            if (s.id.equals(id)) return s;
        }
        throw new IllegalStateException("Unknown spot id: " + id);
    }

    private static long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }
}
