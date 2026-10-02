import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 1: core parking flow")
class Section1Test {

    private static final long HOUR = 60 * 60 * 1000;

    @Test
    @DisplayName("parks vehicles into free spots and reports availability")
    void parksVehiclesAndReportsAvailability() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("M1", SpotSize.MEDIUM),
                new SpotConfig("M2", SpotSize.MEDIUM),
                new SpotConfig("M3", SpotSize.MEDIUM)
        ));

        assertEquals(3, garage.availableSpotsCount());

        Ticket ticketA = garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        assertNotNull(ticketA);
        assertEquals(1, ticketA.spotIds().size());
        assertEquals(2, garage.availableSpotsCount());

        garage.park(new Vehicle("MOTO-B", VehicleType.MOTORCYCLE), 0);
        garage.park(new Vehicle("CAR-C", VehicleType.CAR), 0);
        assertEquals(0, garage.availableSpotsCount());
    }

    @Test
    @DisplayName("rejects parking when the garage is full")
    void rejectsParkingWhenFull() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);

        Ticket result = garage.park(new Vehicle("CAR-D", VehicleType.CAR), 0);
        assertNull(result);
        assertEquals(0, garage.availableSpotsCount());
    }

    @Test
    @DisplayName("rejects double-parking the same plate")
    void rejectsDoubleParking() {
        ParkingGarage garage = new ParkingGarage(List.of(
                new SpotConfig("M1", SpotSize.MEDIUM),
                new SpotConfig("M2", SpotSize.MEDIUM)
        ));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        Ticket second = garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);

        assertNull(second);
        assertEquals(1, garage.availableSpotsCount());
    }

    @Test
    @DisplayName("tracks isParked correctly")
    void tracksIsParked() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        assertFalse(garage.isParked("CAR-A"));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);
        assertTrue(garage.isParked("CAR-A"));
        assertFalse(garage.isParked("UNKNOWN"));
    }

    @Test
    @DisplayName("unparking an unknown plate returns null")
    void unparkingUnknownPlateReturnsNull() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        assertNull(garage.unpark("GHOST", HOUR));
    }

    @Test
    @DisplayName("unparking frees the spot and returns a basic fee")
    void unparkingFreesSpotAndReturnsFee() {
        ParkingGarage garage = new ParkingGarage(List.of(new SpotConfig("M1", SpotSize.MEDIUM)));
        garage.park(new Vehicle("CAR-A", VehicleType.CAR), 0);

        ExitReceipt receipt = garage.unpark("CAR-A", HOUR);
        assertNotNull(receipt);
        assertEquals(5.0, receipt.fee(), 0.0001); // car rate: $5/hr, exactly 1 hour
        assertEquals(1, garage.availableSpotsCount());
        assertFalse(garage.isParked("CAR-A"));
    }
}
