import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 2: order lifecycle")
class Section2Test {

    @Test
    @DisplayName("fulfillOrder moves a PENDING order to FULFILLED and cannot be repeated")
    void fulfillOrderMovesToFulfilled() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));
        Order order = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 2)), 0);

        assertTrue(system.fulfillOrder(order.getId()));
        assertEquals(OrderStatus.FULFILLED, system.getOrder(order.getId()).getStatus());
        assertFalse(system.fulfillOrder(order.getId()));
        assertFalse(system.fulfillOrder("unknown"));
    }

    @Test
    @DisplayName("cancelOrder restocks items, only works from PENDING, and cannot double-restock")
    void cancelOrderRestocksAndOnlyWorksFromPending() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));
        Order order = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 2)), 0);
        assertEquals(3, system.getProduct("A").getStock());

        assertTrue(system.cancelOrder(order.getId()));
        assertEquals(5, system.getProduct("A").getStock());
        assertEquals(OrderStatus.CANCELLED, system.getOrder(order.getId()).getStatus());

        assertFalse(system.cancelOrder(order.getId()));
        assertEquals(5, system.getProduct("A").getStock());
    }

    @Test
    @DisplayName("a fulfilled order cannot be cancelled")
    void fulfilledOrderCannotBeCancelled() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));
        Order order = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 2)), 0);
        system.fulfillOrder(order.getId());

        assertFalse(system.cancelOrder(order.getId()));
        assertEquals(3, system.getProduct("A").getStock());
        assertEquals(OrderStatus.FULFILLED, system.getOrder(order.getId()).getStatus());
    }

    @Test
    @DisplayName("updateProductPrice only affects future orders, not existing ones")
    void updateProductPriceOnlyAffectsFutureOrders() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 10));
        Order order1 = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 1)), 0);

        assertTrue(system.updateProductPrice("A", 20));
        Order order2 = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 1)), 0);

        assertEquals(10.0, system.getOrderTotal(order1.getId()), 0.0001);
        assertEquals(20.0, system.getOrderTotal(order2.getId()), 0.0001);
        assertFalse(system.updateProductPrice("unknown", 5));
        assertFalse(system.updateProductPrice("A", -5));
    }

    @Test
    @DisplayName("getOrderTotal sums unit price * quantity across items, null for unknown orders")
    void getOrderTotalSumsAcrossItems() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 10));
        system.addProduct(new Product("B", "Gadget", 5, 10));
        Order order = system.createOrder("cust-1", List.of(
                new OrderRequestItem("A", 2),
                new OrderRequestItem("B", 3)
        ), 0);

        assertEquals(35.0, system.getOrderTotal(order.getId()), 0.0001);
        assertNull(system.getOrderTotal("nope"));
    }
}
