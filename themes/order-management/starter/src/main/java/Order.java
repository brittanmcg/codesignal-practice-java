import java.util.List;

public class Order {
    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final long createdAt;
    private final String discountCode; // null if no discount was requested
    private final double discountPercent; // 0 if no discount was applied

    public Order(String id, String customerId, List<OrderItem> items, OrderStatus status, long createdAt,
                 String discountCode, double discountPercent) {
        this.id = id;
        this.customerId = customerId;
        this.items = items;
        this.status = status;
        this.createdAt = createdAt;
        this.discountCode = discountCode;
        this.discountPercent = discountPercent;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }
}
