import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Section 4: reporting")
class Section4Test {

    private record Scenario(OrderSystem system, Order order1, Order order2, Order order3) {
    }

    private Scenario buildScenario() {
        OrderSystem system = new OrderSystem();
        system.addProduct(new Product("A", "Widget", 10, 20));
        system.addProduct(new Product("B", "Gadget", 5, 20));
        system.addProduct(new Product("C", "Gizmo", 1, 1));

        Order order1 = system.createOrder("cust-1", List.of(
                new OrderRequestItem("A", 2),
                new OrderRequestItem("B", 1)
        ), 0);
        system.fulfillOrder(order1.getId()); // counts: revenue 25, A:2, B:1

        Order order2 = system.createOrder("cust-1", List.of(new OrderRequestItem("A", 1)), 10);
        system.cancelOrder(order2.getId()); // restocks A, does not count toward revenue/sales

        Order order3 = system.createOrder("cust-2", List.of(new OrderRequestItem("B", 2)), 20);
        // left PENDING — does not count toward revenue/sales

        return new Scenario(system, order1, order2, order3);
    }

    @Test
    @DisplayName("getRevenue only counts FULFILLED orders")
    void getRevenueOnlyCountsFulfilled() {
        Scenario s = buildScenario();
        assertEquals(25.0, s.system().getRevenue(), 0.0001);
    }

    @Test
    @DisplayName("getTopSellingProducts only counts FULFILLED orders, sorted and tie-broken by sku")
    void getTopSellingProductsSortedAndTieBroken() {
        Scenario s = buildScenario();
        assertEquals(List.of(
                new ProductSales("A", 2),
                new ProductSales("B", 1)
        ), s.system().getTopSellingProducts(2));
        assertEquals(List.of(new ProductSales("A", 2)), s.system().getTopSellingProducts(1));
    }

    @Test
    @DisplayName("getLowStockProducts reflects restocks from cancellations, sorted by stock then sku")
    void getLowStockProductsReflectsRestocks() {
        Scenario s = buildScenario();
        // Final stock: A = 20 - 2 - 1 + 1 = 18, B = 20 - 1 - 2 = 17, C = 1 (untouched)
        assertEquals(List.of("C", "B"), s.system().getLowStockProducts(18));
    }

    @Test
    @DisplayName("getCustomerOrderHistory returns all statuses sorted by createdAt")
    void getCustomerOrderHistorySortedByCreatedAt() {
        Scenario s = buildScenario();

        assertEquals(List.of(s.order1().getId(), s.order2().getId()),
                s.system().getCustomerOrderHistory("cust-1").stream().map(Order::getId).toList());
        assertEquals(List.of(s.order3().getId()),
                s.system().getCustomerOrderHistory("cust-2").stream().map(Order::getId).toList());
        assertEquals(List.of(), s.system().getCustomerOrderHistory("nobody"));
    }
}
