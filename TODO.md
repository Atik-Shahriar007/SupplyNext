# SupplyNext — TODO / Current Status

## Completed (Phase A — Architecture & Engineering)

- [x] Full backend CRUD for all 8 modules (Warehouse, Category, Supplier,
      Product, Inventory, PurchaseOrder, SalesOrder, Transfer)
- [x] JWT authentication (register/login) + BCrypt password hashing
- [x] Role-based access control (ADMIN, WAREHOUSE_MANAGER, STAFF)
- [x] Service layer refactor (business logic out of controllers, into
      @Service classes) — applied to all 8 modules
- [x] DTO refactor (Request/Response DTOs, flattened responses, no leaked
      entity internals) — applied to all 8 modules, including User (fixed a
      real password-leak gap) and the 3 line-item modules
- [x] Pagination on every list GET endpoint (backend + frontend Previous/Next
      controls) — applied to all 8 modules
- [x] Swagger/OpenAPI docs (`/swagger-ui.html`) with JWT bearer-auth button
- [x] Global exception handling (clean JSON errors, no stack trace leakage)
- [x] Unit tests: WarehouseService, InventoryService, PurchaseOrderService,
      SalesOrderService, TransferService (all passing)
- [x] Full frontend: login page, protected layout w/ sidebar nav, all 8
      module pages (list + create forms), dashboard summary page
- [x] Dashboard aggregate endpoint + frontend cards

## In Progress / Parked

- [ ] **WarehouseIntegrationTest — DISABLED, needs debugging.**
      File exists at `backend/src/test/java/com/example/scmbackend/warehouse/
      WarehouseIntegrationTest.java`, currently has `@Disabled` annotation.
      Symptom: all 4 test cases return HTTP 403, regardless of expected
      outcome (should be 401 unauthenticated / 200 success-as-admin / 400
      validation-error / 403 wrong-role). Suspected cause: CORS filter
      configuration interacting badly with MockMvc's simulated requests
      (response headers show CORS-related Vary headers even on a plain GET
      with no Origin complexities expected). Attempted fixes that did NOT
      resolve it: adding explicit `Origin: http://localhost:3000` header to
      test requests; adding explicit `authenticationEntryPoint` exception
      handling to force 401 vs 403 distinction. NEXT STEP TO TRY: temporarily
      comment out `.cors(...)` line in SecurityConfig.java's
      securityFilterChain method to confirm/deny CORS as the root cause: if
      tests pass with CORS disabled, find a way to properly configure/mock
      CORS for the test profile specifically (without weakening real prod
      CORS config). This is NOT blocking other work — parked deliberately.

- [ ] Postman collection formalization (base URL + token env variables,
      saved requests per endpoint) — discussed as a good idea, never actually
      built. Currently testing is done ad-hoc per-request in Postman.

## Not Started — Immediate Next Task

**Phase B: Smart Analytics** — this is the next phase to start, and was
chosen as the project's main differentiator (the person's stated favorite
part of the roadmap). Planned features, in this order (agreed order, don't
reshuffle without discussion — later items depend on earlier ones):

1. EOQ (Economic Order Quantity) — formula-based calculation
2. ABC Analysis — classify products by value contribution (A/B/C tiers)
3. Safety Stock calculation
4. Reorder Point / Reorder Suggestions
5. Dead Stock Detection (no movement in X days)
6. Supplier Analytics (performance scoring)
7. KPI Dashboard expansion (more aggregate stats beyond current dashboard)
8. Charts (recharts library, was planned early but never installed/used yet)

None of Phase B has been started — no analytics endpoints, no analytics
frontend page, no recharts installation yet. This is a clean starting point.

## Backlog (Phases C–F, not started)

- Phase C: Python FastAPI microservice for real ML demand forecasting
  (deliberately sequenced AFTER Phase B, since it depends on Phase B's
  historical-data query patterns)
- Phase D: UI/UX polish — toast notifications (replace inline error text),
  skeleton loaders (replace plain "Loading..." text), search/filter on
  tables, status badges (colored pills) instead of plain colored text,
  possibly dark mode
- Phase E: Docker + Docker Compose, GitHub Actions CI/CD, hosting decision
  for backend/frontend/DB (Render/Railway/Fly.io/Oracle Cloud Free Tier were
  discussed as options for the free-tier-friendly path)
- Phase F: README with screenshots, architecture diagram, ER diagram, API
  docs link, possibly a demo video

## Known Tech Debt (deliberate, tracked, not urgent)

- JWT secret key is hardcoded in `JwtUtil.java` — fine for local dev, should
  move to an environment variable before any real deployment
- JWT expiration set to 7 days (bumped from original 10 hours) purely for
  local dev convenience — a real deployment would want short-lived tokens +
  refresh token pattern
- Frontend stores JWT in `localStorage` (readable by JS, XSS-vulnerable in
  theory) rather than an httpOnly cookie — noted as a legitimate future
  "hardening" story, not fixed yet
- `spring.jpa.hibernate.ddl-auto=update` is used for local dev (auto-creates/
  updates tables from entities) — a real deployment would want proper
  migration tooling (e.g. Flyway/Liquibase) instead

## Rules / Conventions for Continuing This Project

- Follow the existing layered pattern (Controller → Service → Repository) and
  DTO pattern (flattened Response DTOs, validated Request DTOs) for any new
  module — don't introduce a different pattern without discussing it first.
- The person is learning Spring Boot as they go — provide full working code
  to copy-paste, with concept explanations alongside, not just terse code
  dumps. Move faster/less-explained through genuinely repetitive patterns
  they've already seen many times (they've explicitly asked for this pace
  adjustment before).
- Always flag deliberate shortcuts/tradeoffs explicitly (security, tech debt)
  rather than silently taking them.
- Test in Postman/browser after each meaningful change; commit to git with a
  descriptive message after each confirmed-working feature.
- The current codebase (GitHub repo) is the source of truth. If this document
  and the actual code ever disagree, trust the code.
- Don't reshuffle the Phase B feature order without discussing — the sequence
  was deliberately chosen (see reasoning in PROJECT_CONTEXT.md's roadmap
  section).