import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 3: buses and the waitlist")
class Section3Test {

    @Test
    @DisplayName("a bus parks across 3 contiguous large spots")
    void busParksAcrossContiguousLargeSpots() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("L1", SpotSize.LARGE),
                new SpotConfig("L2", SpotSize.LARGE),
                new SpotConfig("L3", SpotSize.LARGE),
                new SpotConfig("L4", SpotSize.LARGE)
        ));

        Ticket ticket = garage.park(new Vehicle("BUS-A", VehicleType.BUS), 0);
        assertNotNull(ticket);
        assertEquals(List.of("L1", "L2", "L3"), ticket.spotIds());
        assertEquals(1, garage.availableSpotsCount());
    }

    @Test
    @DisplayName("a bus cannot park across fragmented large spots even if enough are free in total")
    void busCannotParkAcrossFragmentedSpots() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("L1", SpotSize.LARGE),
                new SpotConfig("L2", SpotSize.LARGE),
                new SpotConfig("L3", SpotSize.LARGE),
                new SpotConfig("L4", SpotSize.LARGE),
                new SpotConfig("L5", SpotSize.LARGE),
                new SpotConfig("L6", SpotSize.LARGE)
        ));

        // Fill every spot with a motorcycle (motorcycles fit large spots too), then
        // free every other one so 3 spots are free in total, but none adjacent.
        for (String plate : List.of("M1", "M2", "M3", "M4", "M5", "M6")) {
            garage.park(new Vehicle(plate, VehicleType.MOTORCYCLE), 0);
        }
        garage.unpark("M2", 0);
        garage.unpark("M4", 0);
        garage.unpark("M6", 0);
        assertEquals(3, garage.availableSpotsCount());

        Ticket ticket = garage.park(new Vehicle("BUS-A", VehicleType.BUS), 0);
        assertNull(ticket);
        assertTrue(garage.isWaitlisted("BUS-A"));
    }

    @Test
    @DisplayName("duplicate park attempts are rejected outright, not waitlisted")
    void duplicateParkRejectedNotWaitlisted() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);

        Ticket second = garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        assertNull(second);
        assertFalse(garage.isWaitlisted("CAR-A"));
        assertEquals(List.of(), garage.getWaitlist());
    }

    @Test
    @DisplayName("unparking auto-parks the first waitlisted vehicle that now fits, skipping ones that do not")
    void unparkingAutoParksFirstFittingWaitlisted() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("M1", SpotSize.MEDIUM),
                new SpotConfig("M2", SpotSize.MEDIUM)
        ));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        garage.park(new Vehicle("CAR-B", VehicleType.CAR), 0);

        // Garage is now full and has no large spots at all, so a bus can never fit.
        Ticket busResult = garage.park(new Vehicle("BUS-X", VehicleType.BUS), 0);
        Ticket motoResult = garage.park(new Vehicle("MOTO-Y", VehicleType.MOTORCYCLE), 0);
        assertNull(busResult);
        assertNull(motoResult);
        assertEquals(List.of("BUS-X", "MOTO-Y"), garage.getWaitlist());

        garage.unpark("CAR-A", 0);

        assertTrue(garage.isParked("MOTO-Y"));
        assertFalse(garage.isWaitlisted("MOTO-Y"));
        assertTrue(garage.isWaitlisted("BUS-X"));
        assertEquals(List.of("BUS-X"), garage.getWaitlist());
    }
}
