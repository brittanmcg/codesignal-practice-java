public record Vehicle(String licensePlate, VehicleType type, boolean isVIP) {
    public Vehicle(String licensePlate, VehicleType type) {
        this(licensePlate, type, false);
    }
}
