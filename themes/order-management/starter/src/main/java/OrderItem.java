/** unitPrice is a snapshot of Product.getPrice() at order creation time. */
public record OrderItem(String sku, int quantity, double unitPrice) {
}
