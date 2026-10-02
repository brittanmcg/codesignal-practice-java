// Reference solution — only look at this after finishing (or when truly stuck).
// Drop this in place of src/main/java/OrderSystem.java to compare.

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrderSystem {
    private final Map<String, Product> products = new HashMap<>();
    private final Map<String, Order> orders = new LinkedHashMap<>();
    private final Map<String, Double> discounts = new HashMap<>();
    private int nextOrderNumber = 1;

    // ── Section 1 ──────────────────────────────────────────────────────────

    public boolean addProduct(Product product) {
        if (products.containsKey(product.getSku())) return false;
        products.put(product.getSku(), product);
        return true;
    }

    public Product getProduct(String sku) {
        return products.get(sku);
    }

    public boolean restock(String sku, int quantity) {
        Product product = products.get(sku);
        if (product == null || quantity <= 0) return false;
        product.setStock(product.getStock() + quantity);
        return true;
    }

    public Order createOrder(String customerId, List<OrderRequestItem> items, long createdAt) {
        return createOrder(customerId, items, createdAt, null);
    }

    public Order createOrder(String customerId, List<OrderRequestItem> items, long createdAt, String discountCode) {
        if (items.isEmpty()) return null;

        Set<String> seenSkus = new HashSet<>();
        for (OrderRequestItem item : items) {
            if (seenSkus.contains(item.sku())) return null;
            seenSkus.add(item.sku());

            if (item.quantity() <= 0) return null;

            Product product = products.get(item.sku());
            if (product == null || product.getStock() < item.quantity()) return null;
        }

        double discountPercent = 0;
        if (discountCode != null) {
            Double registered = discounts.get(discountCode);
            if (registered == null) return null;
            discountPercent = registered;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderRequestItem item : items) {
            Product product = products.get(item.sku());
            product.setStock(product.getStock() - item.quantity());
            orderItems.add(new OrderItem(item.sku(), item.quantity(), product.getPrice()));
        }

        Order order = new Order("ORD-" + (nextOrderNumber++), customerId, orderItems, OrderStatus.PENDING,
                createdAt, discountCode, discountPercent);
        orders.put(order.getId(), order);
        return order;
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    // ── Section 2 ──────────────────────────────────────────────────────────

    public boolean fulfillOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING) return false;
        order.setStatus(OrderStatus.FULFILLED);
        return true;
    }

    public boolean cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null || order.getStatus() != OrderStatus.PENDING) return false;

        for (OrderItem item : order.getItems()) {
            Product product = products.get(item.sku());
            if (product != null) product.setStock(product.getStock() + item.quantity());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return true;
    }

    public boolean updateProductPrice(String sku, double newPrice) {
        Product product = products.get(sku);
        if (product == null || newPrice <= 0) return false;
        product.setPrice(newPrice);
        return true;
    }

    public Double getOrderTotal(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) return null;

        double subtotal = 0;
        for (OrderItem item : order.getItems()) {
            subtotal += item.unitPrice() * item.quantity();
        }
        double total = subtotal * (1 - order.getDiscountPercent() / 100);
        return Math.round(total * 100) / 100.0;
    }

    // ── Section 3 ──────────────────────────────────────────────────────────

    public void registerDiscountCode(String code, double percentOff) {
        discounts.put(code, percentOff);
    }

    // ── Section 4 ──────────────────────────────────────────────────────────

    public double getRevenue() {
        double total = 0;
        for (Order order : orders.values()) {
            if (order.getStatus() == OrderStatus.FULFILLED) {
                total += getOrderTotal(order.getId());
            }
        }
        return Math.round(total * 100) / 100.0;
    }

    public List<ProductSales> getTopSellingProducts(int limit) {
        Map<String, Integer> unitsBySku = new HashMap<>();
        for (Order order : orders.values()) {
            if (order.getStatus() != OrderStatus.FULFILLED) continue;
            for (OrderItem item : order.getItems()) {
                unitsBySku.merge(item.sku(), item.quantity(), Integer::sum);
            }
        }

        List<ProductSales> sales = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : unitsBySku.entrySet()) {
            sales.add(new ProductSales(entry.getKey(), entry.getValue()));
        }
        sales.sort((a, b) -> {
            if (b.unitsSold() != a.unitsSold()) return b.unitsSold() - a.unitsSold();
            return a.sku().compareTo(b.sku());
        });
        return sales.subList(0, Math.min(limit, sales.size()));
    }

    public List<String> getLowStockProducts(int threshold) {
        List<Product> lowStock = new ArrayList<>();
        for (Product p : products.values()) {
            if (p.getStock() < threshold) lowStock.add(p);
        }
        lowStock.sort((a, b) -> {
            if (a.getStock() != b.getStock()) return a.getStock() - b.getStock();
            return a.getSku().compareTo(b.getSku());
        });

        List<String> skus = new ArrayList<>();
        for (Product p : lowStock) skus.add(p.getSku());
        return skus;
    }

    public List<Order> getCustomerOrderHistory(String customerId) {
        List<Order> history = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getCustomerId().equals(customerId)) history.add(order);
        }
        history.sort((a, b) -> Long.compare(a.getCreatedAt(), b.getCreatedAt()));
        return history;
    }
}
