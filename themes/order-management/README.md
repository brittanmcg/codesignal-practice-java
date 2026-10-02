# Assessment: Order & Inventory Management System

**Estimated total time:** 70–90 minutes across 4 sections.

## How to work this assessment

- Work in order; don't read a later section until the current one's tests pass.
- Implement everything in `src/main/java/OrderSystem.java`. Method signatures are already declared —
  fill in the bodies. Don't change signatures or the other files in `src/main/java/` (`Product`,
  `Order`, `OrderItem`, `OrderRequestItem`, `OrderStatus`, `ProductSales`).
- Run only the section you're on, from inside your attempt's directory:

  ```bash
  cd attempts/<your-attempt-dir>
  mvn test -Dtest=Section1Test
  ```

  All four test classes should pass together by the end (`mvn test`) — later sections deepen the same
  class.
- Don't look at `reference-solution/` until you're done or stuck.

---

## Domain model

Products have a `sku`, `name`, `price`, and `stock` count. Orders are placed by a `customerId`
against a list of `OrderRequestItem(sku, quantity)` request items.

A core rule that applies from the start, even before later sections test it directly: **order
creation is atomic**. If anything about the request is invalid — a bad sku, insufficient stock, a
bad quantity, a bad discount code — the *entire* order is rejected (`null`) and **no stock is
deducted**. There's no partial fulfillment.

Order IDs are assigned as `"ORD-1"`, `"ORD-2"`, ... in the order orders are **successfully** created
(rejected attempts don't consume an id).

`createOrder` is overloaded: a 3-argument version (no discount code) and a 4-argument version that
takes a `discountCode`. Both end up at the same logic — don't change either signature.

---

## Section 1 — Inventory and basic orders (easy)

Implement:
- `addProduct(product)` — registers a new product. Returns `false` (and does nothing) if the sku is
  already registered, `true` otherwise.
- `getProduct(sku)` — returns the product, or `null` if unknown.
- `restock(sku, quantity)` — increases stock by `quantity`. Returns `false` if the sku is unknown or
  `quantity <= 0`.
- `createOrder(customerId, items, createdAt)` — validates every item's sku exists and there's enough
  stock for all items *before* deducting anything, then deducts stock and returns the new `Order`
  (status `PENDING`). Returns `null` on any validation failure. No discount codes are registered yet
  in this section, so just use the 3-argument overload.
- `getOrder(orderId)` — returns the order, or `null` if unknown.

## Section 2 — Order lifecycle (medium)

Implement:
- `fulfillOrder(orderId)` — moves a `PENDING` order to `FULFILLED`. Returns `false` (no change) if
  the order doesn't exist or isn't currently `PENDING`.
- `cancelOrder(orderId)` — moves a `PENDING` order to `CANCELLED` **and restocks every item**.
  Returns `false` (no change) if the order doesn't exist or isn't currently `PENDING` — you can't
  cancel a fulfilled or already-cancelled order.
- `updateProductPrice(sku, newPrice)` — updates a product's price for *future* orders only. Existing
  orders keep the unit price they were created with (`OrderItem.unitPrice()` is a snapshot taken at
  creation time — make sure `createOrder` actually snapshots it rather than reading the live price
  later).
- `getOrderTotal(orderId)` — `sum(unitPrice * quantity)` across the order's items as a `Double`,
  `null` if the order doesn't exist. No discount math yet (Section 3).

## Section 3 — Discounts and stricter validation (medium-hard)

Implement:
- `registerDiscountCode(code, percentOff)` — registers a reusable discount code.
- `createOrder`'s 4-argument overload now honors `discountCode`: if registered, the order's total (in
  `getOrderTotal`) is reduced by that percentage, and `Order.getDiscountPercent()` /
  `Order.getDiscountCode()` reflect it. If `discountCode` is passed but **not registered, the entire
  order is rejected** (`null`, no stock deducted) — same atomicity rule as always.
- Tighten `createOrder` validation: reject (whole order, atomically) if any requested `quantity` is
  not positive, or if the same `sku` appears more than once in the same request's item list.
- `getOrderTotal` rounds to 2 decimal places (avoid floating point noise, e.g. `19.999999999`).

## Section 4 — Reporting (hard)

Implement, all read-only over existing order/product history:
- `getRevenue()` — sum of post-discount totals for **`FULFILLED` orders only**. Pending and
  cancelled orders don't count.
- `getTopSellingProducts(limit)` — a `List<ProductSales>`, counting units from `FULFILLED` orders
  only, sorted by `unitsSold` descending, ties broken by `sku` ascending, truncated to `limit`.
- `getLowStockProducts(threshold)` — skus with `stock < threshold`, sorted by stock ascending, ties
  broken by `sku` ascending.
- `getCustomerOrderHistory(customerId)` — all of a customer's orders (any status), sorted by
  `createdAt` ascending.

Watch the interaction with earlier sections: a cancelled order restocks its items (affects
`getLowStockProducts`) but must **not** count toward revenue or top-selling — that's the edge case
most likely to trip up an otherwise-working solution.
