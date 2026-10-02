import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 4: analytics and edge cases")
class Section4Test {

    private static final long HOUR = 60 * 60 * 1000;

    @Test
    @DisplayName("getTotalRevenue sums fees across completed visits")
    void totalRevenueSumsFees() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("M1", SpotSize.MEDIUM),
                new SpotConfig("M2", SpotSize.MEDIUM)
        ));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        garage.park(new Vehicle("CAR-B", VehicleType.CAR), 0);
        garage.unpark("CAR-A", HOUR); // fee 5
        garage.unpark("CAR-B", 2 * HOUR); // fee 10

        assertEquals(15.0, garage.getTotalRevenue(), 0.0001);
    }

    @Test
    @DisplayName("getRevenueByVehicleType breaks fees down per type, 0 for unused types")
    void revenueByVehicleTypeBreaksDown() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("L1", SpotSize.LARGE),
                new SpotConfig("L2", SpotSize.LARGE)
        ));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        garage.park(new Vehicle("MOTO-A", VehicleType.MOTORCYCLE), 0);
        garage.unpark("CAR-A", HOUR); // 5
        garage.unpark("MOTO-A", HOUR); // 2

        Map<VehicleType, Double> byType = garage.getRevenueByVehicleType();
        assertEquals(5.0, byType.get(VehicleType.CAR), 0.0001);
        assertEquals(2.0, byType.get(VehicleType.MOTORCYCLE), 0.0001);
        assertEquals(0.0, byType.get(VehicleType.BUS), 0.0001);
    }

    @Test
    @DisplayName("getAverageDurationMs averages completed visit durations")
    void averageDurationAveragesVisits() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        garage.unpark("CAR-A", HOUR);
        garage.park(new Vehicle("CAR-B", VehicleType.CAR), 0);
        garage.unpark("CAR-B", 3 * HOUR);

        assertEquals(2.0 * HOUR, garage.getAverageDurationMs(), 0.0001);
    }

    @Test
    @DisplayName("getBusiestVehicleType returns the type with the most completed visits, null with no history")
    void busiestVehicleTypeReturnsMostCommon() {
        ParkingGarage empty = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        assertNull(empty.getBusiestVehicleType());

        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        garage.unpark("CAR-A", HOUR);
        garage.park(new Vehicle("CAR-B", VehicleType.CAR), 0);
        garage.unpark("CAR-B", HOUR);
        garage.park(new Vehicle("MOTO-A", VehicleType.MOTORCYCLE), 0);
        garage.unpark("MOTO-A", HOUR);

        assertEquals(VehicleType.CAR, garage.getBusiestVehicleType());
    }

    @Test
    @DisplayName("getPeakVehicleCount reflects the historical max, not just current occupancy")
    void peakVehicleCountReflectsHistoricalMax() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("M1", SpotSize.MEDIUM),
                new SpotConfig("M2", SpotSize.MEDIUM),
                new SpotConfig("M3", SpotSize.MEDIUM)
        ));

        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        garage.park(new Vehicle("CAR-B", VehicleType.CAR), 0);
        garage.park(new Vehicle("CAR-C", VehicleType.CAR), 0); // peak = 3

        garage.unpark("CAR-A", HOUR);
        garage.unpark("CAR-B", HOUR); // current = 1

        assertEquals(3, garage.getPeakVehicleCount());
    }
}
