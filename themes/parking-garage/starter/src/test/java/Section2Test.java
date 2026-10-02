import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 2: size matching and billing")
class Section2Test {

    private static final long HOUR = 60 * 60 * 1000;

    @Test
    @DisplayName("a motorcycle prefers the smallest available compatible spot")
    void motorcyclePrefersSmallestSpot() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("S1", SpotSize.SMALL),
                new SpotConfig("M1", SpotSize.MEDIUM),
                new SpotConfig("L1", SpotSize.LARGE)
        ));

        Ticket ticket = garage.park(new Vehicle("MOTO-A", VehicleType.MOTORCYCLE), 0);
        assertEquals(List.of("S1"), ticket.spotIds());
    }

    @Test
    @DisplayName("a car never takes a small spot, even if available")
    void carNeverTakesSmallSpot() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("S1", SpotSize.SMALL),
                new SpotConfig("S2", SpotSize.SMALL),
                new SpotConfig("L1", SpotSize.LARGE)
        ));

        Ticket ticket = garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        assertEquals(List.of("L1"), ticket.spotIds());
    }

    @Test
    @DisplayName("a car prefers medium over large")
    void carPrefersMediumOverLarge() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("M1", SpotSize.MEDIUM),
                new SpotConfig("L1", SpotSize.LARGE)
        ));

        Ticket ticket = garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        assertEquals(List.of("M1"), ticket.spotIds());
    }

    @Test
    @DisplayName("billing rounds partial hours up")
    void billingRoundsPartialHoursUp() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("L1", SpotSize.LARGE)));
        garage.park(new Vehicle("MOTO-A", VehicleType.MOTORCYCLE), 0);

        ExitReceipt receipt = garage.unpark("MOTO-A", 90 * 60 * 1000); // 1.5 hours
        assertEquals(4.0, receipt.fee(), 0.0001); // ceil(1.5) = 2 hours * $2/hr
    }

    @Test
    @DisplayName("billing has a 1-hour minimum even for very short stays")
    void billingHasOneHourMinimum() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);

        ExitReceipt receipt = garage.unpark("CAR-A", 5 * 60 * 1000); // 5 minutes
        assertEquals(5.0, receipt.fee(), 0.0001);
    }

    @Test
    @DisplayName("VIP vehicles get a 20% discount on the fee")
    void vipVehiclesGetDiscount() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        garage.park(new Vehicle("CAR-VIP", VehicleType.CAR, true), 0);

        ExitReceipt receipt = garage.unpark("CAR-VIP", HOUR);
        assertEquals(4.0, receipt.fee(), 0.0001); // 5 * 0.8
    }
}
