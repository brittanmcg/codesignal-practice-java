import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 1: inventory and basic orders")
class Section1Test {

    @Test
    @DisplayName("addProduct registers a product and rejects duplicate skus")
    void addProductRegistersAndRejectsDuplicates() {
        OrderSystem system = new OrderSystem();
        assertTrue(system.addProduct(new Product("A", "Widget", 10, 5)));
        assertFalse(system.addProduct(new Product("A", "Widget v2", 12, 1)));
        assertEquals(10, system.getProduct("A").getPrice(), 0.0001);
    }

    @Test
    @DisplayName("getProduct returns null for unknown skus")
    void getProductReturnsNullForUnknown() {
        OrderSystem system = new OrderSystem();
        assertNull(system.getProduct("NOPE"));
    }

    @Test
    @DisplayName("restock increases stock and rejects invalid input")
    void restockIncreasesStockAndRejectsInvalid() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));

        assertTrue(system.restock("A", 3));
        assertEquals(8, system.getProduct("A").getStock());
        assertFalse(system.restock("UNKNOWN", 3));
        assertFalse(system.restock("A", -1));
    }

    @Test
    @DisplayName("createOrder deducts stock and returns a PENDING order")
    void createOrderDeductsStockAndReturnsPendingOrder() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));

        Order order = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 2)), 1000);
        assertNotNull(order);
        assertEquals("ORD-1", order.getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(List.of(new OrderItem("A", 2, 10)), order.getItems());
        assertEquals(3, system.getProduct("A").getStock());
    }

    @Test
    @DisplayName("createOrder is atomic: insufficient stock on one item rejects the whole order")
    void createOrderIsAtomicOnInsufficientStock() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));
        system.addProduct(new Product("B", "Gadget", 20, 1));

        Order order = system.createOrder("cust-1", List.of(
                new OrderRequestItem("A", 2),
                new OrderRequestItem("B", 5)
        ), 1000);

        assertNull(order);
        assertEquals(5, system.getProduct("A").getStock());
        assertEquals(1, system.getProduct("B").getStock());
    }

    @Test
    @DisplayName("createOrder rejects an unknown sku without mutating anything")
    void createOrderRejectsUnknownSkuWithoutMutating() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));

        Order order = system.createOrder("cust-1", List.of(
                new OrderRequestItem("A", 1),
                new OrderRequestItem("ZZZ", 1)
        ), 1000);

        assertNull(order);
        assertEquals(5, system.getProduct("A").getStock());
    }

    @Test
    @DisplayName("getOrder returns null for unknown ids and the order for known ones")
    void getOrderReturnsNullOrKnownOrder() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 5));
        Order created = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 1)), 1000);

        assertNull(system.getOrder("nope"));
        assertSame(created, system.getOrder(created.getId()));
    }
}
