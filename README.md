# CodeSignal-style Assessment Practice (Java)

Java sibling of [`codesignal-practice`](../codesignal-practice) (the TypeScript version). Same idea: a
reusable scaffold for practicing multi-section, project-style coding assessments — one evolving
codebase, split into sections of increasing difficulty, where you write code to make a growing test
suite pass. Same four themes, same specs, ported to Java + Maven + JUnit 5.

## Setup (one-time)

You need a JDK (17+) and Maven on your `PATH`:

```bash
java -version
mvn -version
```

On macOS with Homebrew: `brew install openjdk maven`.

## Starting a practice run

```bash
java scripts/NewAttempt.java
```

This lists the available themes and lets you pick one interactively. To skip straight to a theme:

```bash
java scripts/NewAttempt.java parking-garage
java scripts/NewAttempt.java --list
```

It copies that theme's starter into a fresh timestamped folder under `attempts/` — each attempt is a
self-contained Maven project (own `pom.xml`) — and prints exactly what to read and run next.

## Working an attempt

1. Open `themes/<theme-id>/README.md` — read **only** the first section for now. Treat this like the
   real thing: don't read ahead.
2. Implement in `attempts/<your-attempt>/src/main/java/*.java`. Method signatures are already stubbed
   with `TODO` comments tagged by section — don't change the signatures or the record/enum files.
3. From inside the attempt directory, run just that section's tests:
   ```bash
   cd attempts/<your-attempt>
   mvn test -Dtest=Section1Test
   ```
4. Once it's green, read the next section and keep going. By the end, `mvn test` should pass all four
   test classes together — later sections deepen the same class rather than replacing it.
5. If you get stuck, `themes/<theme-id>/reference-solution/` has a full solution you can diff
   against — but treat it as a last resort, same as you would in a real assessment.

Make sure you run `mvn` from **inside** the attempt directory (where its `pom.xml` is), not from the
repo root.

## Available themes

- **`parking-garage`** — OOP simulation. Size-matched spot assignment, billing rules, a waitlist with
  FIFO-skip logic, and analytics.
- **`order-management`** — CRUD + business rules. Atomic order validation, order lifecycle
  (fulfill/cancel with restocking), discount codes, and reporting.
- **`rate-limiter`** — Algorithmic. Fixed-window and sliding-window-log limiting as two independent
  algorithms, per-client overrides, a block list, and usage analytics.
- **`lru-cache`** — Data structure design. Capacity-based LRU eviction, TTL expiration, recency
  introspection, and cache analytics. Generic (`Cache<V>`).

All four are pure in-memory Java (no framework, no I/O), in the default package to keep the focus on
logic, edge cases, and test-driven implementation.

## Differences from the TypeScript version

- `null` replaces `undefined`/`| null` returns; nullable numbers use boxed types (`Double`).
- Optional parameters become overloads (`createOrder` with/without a discount code, `Cache.set`
  with/without a TTL).
- The order-management "non-integer quantity" check doesn't exist — `int` can't hold `1.5`.

## Adding a new theme

```
themes/<new-id>/
  META.json              # {id, name, description, estimatedMinutes, sections: [...]}
  README.md              # full 4-section spec, one section at a time
  starter/
    pom.xml               # copy from an existing theme, change <artifactId>
    src/main/java/        # stubbed class(es) + records/enums, TODOs tagged by section
    src/test/java/        # Section1Test.java ... Section4Test.java
  reference-solution/     # full working solution file(s), same names as starter/src/main/java
```

Verify a new theme by copying `starter/` into `attempts/`, confirming `mvn test` fails with "Not
implemented", then swapping in the reference solution and confirming everything passes.
