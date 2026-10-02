# Assessment: Parking Garage System

**Estimated total time:** 70–90 minutes across 4 sections.

## How to work this assessment

- Treat this like a real CodeSignal General Coding Assessment: work in order, don't read ahead to
  later sections until the current section's tests pass.
- Implement everything in `src/main/java/ParkingGarage.java`. The public method signatures are
  already declared for you (this mirrors CodeSignal's pattern of giving you the interface up front) —
  you fill in the bodies. Don't change method signatures or the other files in `src/main/java/`
  (`SpotConfig`, `Vehicle`, `Ticket`, `ExitReceipt`, `VehicleType`, `SpotSize`).
- Run only the section you're on, from inside your attempt's directory:

  ```bash
  cd attempts/<your-attempt-dir>
  mvn test -Dtest=Section1Test
  ```

  Once it's green, move to the next section. By the end, all four test classes should pass together
  (`mvn test`) — later sections deepen the same class, they don't replace it.
- No peeking at `reference-solution/` until you're done or truly stuck. It's there to unblock you or
  to compare approaches afterward, not to read first.

---

## Domain model

A garage has a fixed list of parking spots, each with an `id` and a `size` (`SpotSize.SMALL`,
`SpotSize.MEDIUM`, or `SpotSize.LARGE`). Vehicles are `VehicleType.MOTORCYCLE`, `VehicleType.CAR`, or
`VehicleType.BUS`.

Fit rules (apply from the start, even though earlier sections won't exercise all of them):
- **motorcycle** fits in `SMALL`, `MEDIUM`, or `LARGE` — prefer the smallest available spot first.
- **car** fits in `MEDIUM` or `LARGE` — prefer `MEDIUM` first.
- **bus** needs **3 contiguous `LARGE` spots** (contiguous = adjacent entries in the spot list you
  passed into the constructor, all size `LARGE` and all free).

Hourly rates: motorcycle `$2`, car `$5`, bus `$15`. Billing always rounds **up** to the next full
hour, minimum 1 hour.

VIP vehicles (`vehicle.isVIP() == true`) get a **20% discount** on the fee.

---

## Section 1 — Core parking flow (easy)

Implement:
- `park(vehicle, entryTime)` — assigns a free, size-compatible spot and returns a `Ticket`, or
  returns `null` if the vehicle is already parked or no compatible spot is free.
- `unpark(licensePlate, exitTime)` — frees the vehicle's spot(s) and returns an `ExitReceipt`
  (including the correct fee for the vehicle's type and duration), or `null` if that plate isn't
  currently parked.
- `availableSpotsCount()` — number of free spots.
- `isParked(licensePlate)` — whether that plate currently occupies a spot.

For this section you can assume simple cases: motorcycles and cars only, plenty of same-size spots.
Get the happy path, the "garage is full" path, and the "double park" / "unpark unknown plate" edge
cases right.

## Section 2 — Size matching and accurate billing (medium)

Now the tests exercise mixed spot sizes and mixed vehicle types (still motorcycle/car). Make sure:
- A motorcycle takes the smallest available compatible spot before a larger one.
- A car only takes `MEDIUM`/`LARGE`, never `SMALL`.
- Fees are calculated per the rate table above, always rounding partial hours **up**, with a 1-hour
  minimum even for very short stays.
- VIP discount is applied correctly.

## Section 3 — Buses and a waitlist (medium-hard)

Add:
- Bus support: a bus can only park if 3 **contiguous** `LARGE` spots are free. If the large spots are
  fragmented (e.g., free-occupied-free), the bus should **not** park even if 3+ large spots are free
  in total.
- A **waitlist**: if `park()` can't find a compatible spot (or contiguous block, for buses), instead
  of just rejecting, add the vehicle to a FIFO waitlist. Expose:
  - `getWaitlist()` — plates currently waiting, in FIFO order.
  - `isWaitlisted(licensePlate)` — whether a plate is currently waiting.
- On `unpark()`, after freeing the spot(s), scan the waitlist **in FIFO order** and auto-park the
  first vehicle that now fits (not necessarily the head — skip over ones that don't fit and keep
  checking). Keep doing this while spots remain and someone in the waitlist fits. Auto-parked
  vehicles get an `entryTime` equal to the `exitTime` that freed their spot.
- A duplicate `park()` call for an already-parked plate is still a plain rejection (`null`), not a
  waitlist addition.

## Section 4 — Analytics and edge cases (hard)

Add read-only analytics methods, computed from garage history:
- `getTotalRevenue()` — sum of all fees ever collected.
- `getRevenueByVehicleType()` — `Map<VehicleType, Double>` of fees collected per vehicle type (`0`
  for types with no completed visits — the map should contain all three keys regardless).
- `getAverageDurationMs()` — average parked duration (ms) across all completed visits, `0` if none.
- `getBusiestVehicleType()` — the vehicle type with the most completed visits, `null` if there's no
  history yet. Ties can be broken by whichever type you check first — tests avoid exact ties.
- `getPeakVehicleCount()` — the highest number of vehicles parked at the same time, at any point in
  the garage's history (including vehicles still parked "now").

Also handle the fragmentation and FIFO-skip edge cases precisely — this section's tests are the ones
most likely to catch a solution that "mostly" works.
