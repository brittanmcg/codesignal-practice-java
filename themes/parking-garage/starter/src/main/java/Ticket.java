import java.util.List;

public record Ticket(String licensePlate, VehicleType vehicleType, List<String> spotIds, long entryTime,
                      boolean isVIP) {
}
