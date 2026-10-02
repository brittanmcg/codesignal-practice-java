import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 3: discounts and stricter validation")
class Section3Test {

    @Test
    @DisplayName("a registered discount code reduces the order total, rounded to 2 decimals")
    void registeredDiscountCodeReducesTotal() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 9.99, 10));
        system.registerDiscountCode("SAVE10", 10);

        Order order = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 3)), 0, "SAVE10");
        assertNotNull(order);
        assertEquals("SAVE10", order.getDiscountCode());
        assertEquals(10.0, order.getDiscountPercent(), 0.0001);
        assertEquals(26.97, system.getOrderTotal(order.getId()), 0.0001); // 29.97 * 0.9
    }

    @Test
    @DisplayName("an unregistered discount code rejects the whole order atomically")
    void unregisteredDiscountCodeRejectsOrder() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));

        Order order = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 2)), 0, "NOPE");
        assertNull(order);
        assertEquals(5, system.getProduct("A").getStock());
    }

    @Test
    @DisplayName("a duplicate sku within one order request is rejected atomically")
    void duplicateSkuWithinRequestRejected() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));

        Order order = system.createOrder("cust-1", List.of(
                new OrderRequestItem("A", 1),
                new OrderRequestItem("A", 2)
        ), 0);
        assertNull(order);
        assertEquals(5, system.getProduct("A").getStock());
    }

    @Test
    @DisplayName("invalid quantities (zero or negative) are rejected atomically")
    void invalidQuantitiesRejected() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));

        assertNull(system.createOrder("cust-1", List.of(new OrderRequestItem("A", 0)), 0));
        assertNull(system.createOrder("cust-1", List.of(new OrderRequestItem("A", -1)), 0));
        assertEquals(5, system.getProduct("A").getStock());
    }
}
