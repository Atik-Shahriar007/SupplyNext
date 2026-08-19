# SupplyNext — Project Context

## Overview
SupplyNext is a full-stack, multi-warehouse Smart Supply Chain Management System.
Built solo, from scratch, as a portfolio/resume project — the person building it
(Atik Shahriar) had zero Spring Boot experience at project start and is learning
as they go. Goal: a genuinely "wow"-level, resume-quality project — not a toy CRUD
demo, but real business logic, real auth, real testing, real architecture.

The person building this is dedicated and has no deadline — quality and depth are
prioritized over speed. They explicitly want deep analytics/smart features (EOQ,
ABC analysis, real ML demand forecasting) as the project's main differentiator.

## Repository Structure (monorepo)
SupplyNext/
├── backend/ ← Spring Boot (Java)
├── frontend/ ← Next.js (TypeScript)
├── .gitignore
├── PROJECT_CONTEXT.md
└── TODO.md
GitHub: Atik-Shahriar007/SupplyNext (branch: main)
Both frontend and backend share ONE git history (root-level .git). This required a
manual fix early on — originally each had separate .git folders, they were merged
into one root-level repo via `git add -A` rename detection.

## Tech Stack

**Backend:**
- Java 25 (LTS) — installed at `C:\Users\atmsh\.jdk\jdk-25` on the dev machine
- Spring Boot 4.1.0 (NOTE: this is a very new/cutting-edge version — some class
  package paths differ from older Spring Boot docs/tutorials — see "Known Quirks"
  below)
- Maven (using mvnw wrapper)
- PostgreSQL (local dev: database `scm_db`, user `postgres`, password `1234`)
- Spring Data JPA + Hibernate
- Spring Security + JWT (jjwt library, v0.12.6)
- Lombok (@Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor on entities)
- SpringDoc OpenAPI (Swagger) — springdoc-openapi-starter-webmvc-ui v2.8.5
- Testing: JUnit 5, Mockito, H2 (in-memory DB for tests), spring-security-test

**Frontend:**
- Next.js 16.3.0 (App Router, TypeScript, Turbopack, src/ directory)
- Tailwind CSS
- shadcn/ui — IMPORTANT: using the **Base UI** component library preset (not Radix,
  not React Aria), preset name "Nova" (Lucide icons + Geist font)
- react-hook-form + zod + @hookform/resolvers (form validation)
- Axios (API client, with JWT auto-attach interceptor)
- lucide-react (icons)

**Dev machine:** Windows 11, IntelliJ IDEA (backend), VS Code (frontend), Postman
(API testing), pgAdmin (DB GUI), PowerShell.

## Known Quirks / Gotchas (important — don't relearn these the hard way)

1. **Spring Boot 4.1's `@AutoConfigureMockMvc` moved package.** It is NOT at
   `org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc`
   (older Spring Boot / most tutorials/docs). It is actually at:
   `org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc`
   Discovered by searching the actual jar contents in .m2 repo. Expect other
   Spring Boot 4.1 classes to potentially have moved too — verify via IntelliJ
   symbol search or jar inspection rather than trusting older documentation/training
   data if something "should exist" but doesn't resolve.

2. **shadcn's `form` component is DEPRECATED / removed from the registry.**
   Do NOT use `<Form>`, `<FormField>`, `<FormItem>` etc. Use the `Field` primitives
   instead: `Field`, `FieldLabel`, `FieldError` (from `@/components/ui/field`),
   combined with react-hook-form directly. Pattern used throughout this project:
```tsx
   <Field>
     <FieldLabel htmlFor="name">Name</FieldLabel>
     <Input id="name" {...register("name")} />
     <FieldError errors={[errors.name]} />
   </Field>
```

3. **`user` is a reserved word in PostgreSQL.** The User entity is mapped to table
   `app_user` via `@Table(name = "app_user")`, not the default `user`.

4. **react-hook-form + shadcn Select components need `Controller`, not `register`.**
   Since Select isn't a native `<select>`, wire it via:
```tsx
   <Controller name="categoryId" control={control} render={({ field }) => (
     <Select onValueChange={field.onChange} value={field.value}>...
```
   ALSO: every useForm() call MUST include `defaultValues` for every field
   (including Select-backed fields), or React throws an
   "uncontrolled to controlled" warning/crash the first time a value changes.

5. **JWT token expiration was bumped from 10 hours to 7 days** for local dev
   convenience (`JwtUtil.java`, `EXPIRATION_TIME`). This is explicitly a dev-only
   convenience — flagged as something to revisit before real deployment (shorter
   tokens + refresh token pattern would be the production-correct approach).
   The JWT secret key is also currently HARDCODED in JwtUtil.java — also flagged
   as a pre-deployment fix (should come from env variable).

6. **JAVA_HOME / PATH environment variable mismatch caused Maven CLI failures**
   (IntelliJ used Java 25 internally, but system PATH pointed to an old Java 17
   install with a leading-space formatting bug in the JAVA_HOME value). Fixed by
   correctly setting JAVA_HOME to `C:\Users\atmsh\.jdk\jdk-25` and removing
   the old Java 17 entry from PATH. If Maven CLI (`./mvnw`) ever fails with
   Java-version-related errors again, check this first.

7. **Postman note:** pasting URLs sometimes introduces a trailing newline
   character (shows as `%0A` in error paths) — if a request 404s unexpectedly,
   check for/clear this by retyping the URL manually.

8. **Pagination pattern:** ALL list GET endpoints are paginated
   (`?page=0&size=10` query params, Spring's `Page<T>`/`Pageable`/`PageRequest`).
   Frontend pages that need "all" options for dropdowns (e.g. Product page needing
   all Categories) call those endpoints with `?size=100` as a pragmatic
   workaround rather than building separate non-paginated endpoints.

9. **CORS + Spring Security integration test issue (UNRESOLVED, see TODO.md)** —
   WarehouseIntegrationTest currently fails/disabled: MockMvc requests were
   getting 403 across the board (including cases that should be 401 or 200).
   Root cause not fully confirmed — suspected interaction between the CORS
   filter config and MockMvc's simulated requests in the test security context.
   Parked, not blocking other work.

## Backend Architecture

**Layering pattern used consistently across ALL 8 modules:**

Controller (HTTP only) → Service (business logic) → Repository (JPA/DB)

Controllers NEVER contain business logic directly — always delegate to a
`@Service`-annotated class. Services throw plain `RuntimeException` with a
human-readable message for business-rule violations (e.g. "Insufficient stock:
cannot go below 0", "This purchase order was already received") — these are
caught centrally (see Global Exception Handling below), never handled with
manual `ResponseEntity.badRequest()` inside services.

**DTO pattern used consistently:**
Every entity has:
- `<Entity>RequestDto` — validated input shape (Bean Validation annotations:
  @NotBlank, @NotNull, @Positive, @Email, etc. live HERE, not on the entity)
- `<Entity>ResponseDto` — output shape, FLATTENED (no nested full objects —
  e.g. Product's response has `categoryId`, `categoryName` as flat fields,
  not a nested `category: {...}` object). This was a deliberate refactor from
  an earlier version that DID nest full entities (which leaked things like
  a full User's hashed password, and created tight coupling).
- Services have a private `toResponseDto(Entity e)` helper method for the
  entity → DTO conversion, reused across getAll/create/etc.
- For entities with line-items (PurchaseOrder, SalesOrder, Transfer), the
  item DTO (e.g. `PurchaseOrderItemDto`) doubles as both request and response
  shape (id/productName are null on the way in, populated on the way out).

**Global exception handling:** `common/GlobalExceptionHandler.java`
(`@RestControllerAdvice`) — catches:
- `MethodArgumentNotValidException` → clean field-level error map
- `RuntimeException` (our manually thrown business errors) → clean
  `ErrorResponse` object (timestamp, status, error, message, path)
- Generic `Exception` → safe generic 500 message (no stack trace leakage)

**Pagination:** every `getAll...` method takes a `Pageable` param, controllers
accept `?page=0&size=10` query params via `@RequestParam(defaultValue=...)`.

**Auth:** JWT-based, stateless (`SessionCreationPolicy.STATELESS`).
- `POST /api/auth/register` — hashes password (BCrypt), defaults role to STAFF
  if not provided, returns `UserResponseDto` (id/username/role — NEVER password)
- `POST /api/auth/login` — returns `{token, role}`
- `JwtAuthFilter` — reads `Authorization: Bearer <token>` header, validates,
  sets Spring Security context
- Roles: ADMIN, WAREHOUSE_MANAGER, STAFF (plain string field on User entity,
  not a separate Role entity/table — deliberate simplification)
- RBAC rules (in `SecurityConfig.java`):
  - `/api/auth/**` — public
  - `/v3/api-docs/**`, `/swagger-ui/**` — public
  - POST to warehouses/categories/suppliers/products, POST/PATCH to
    purchase-orders/transfers/inventory-adjust → ADMIN or WAREHOUSE_MANAGER only
  - POST/PATCH sales-orders → ADMIN, WAREHOUSE_MANAGER, or STAFF
  - All GET requests just require authentication (any logged-in role)
  - Everything else → authenticated() catch-all

## Backend Modules (all 8, all follow the same layered+DTO pattern above)

1. **warehouse** — Warehouse(id, name, location, capacity)
2. **category** — Category(id, name, description)
3. **supplier** — Supplier(id, name, contactPerson, phone, email, address)
4. **product** — Product(id, sku, name, description, price, category→Category,
   supplier→Supplier) — ManyToOne relationships
5. **inventory** — Inventory(id, product→Product, warehouse→Warehouse, quantity)
   - Has `findByProductIdAndWarehouseId` derived query method (used heavily by
     PO/SO/Transfer services)
   - `PATCH /api/inventory/{id}/adjust` — manual stock adjustment, body
     `{change: <int>}`, rejects if result would go negative
6. **purchaseorder** — PurchaseOrder(id, supplier, warehouse, orderDate, status,
   items: List<PurchaseOrderItem>) — OneToMany with PurchaseOrderItem
   - `PATCH /{id}/receive` — increases inventory (creates Inventory record if
     none exists for that product+warehouse combo), rejects if already RECEIVED
7. **salesorder** — SalesOrder(id, customerName, warehouse, orderDate, status,
   items: List<SalesOrderItem>)
   - `PATCH /{id}/ship` — TWO-PASS validation: first checks ALL items have
     sufficient stock, THEN reduces stock (prevents partial-failure states).
     Rejects if already SHIPPED or if any item has insufficient stock.
8. **transfer** — Transfer(id, fromWarehouse, toWarehouse, transferDate, status,
   items: List<TransferItem>)
   - `PATCH /{id}/complete` — decreases source warehouse stock, increases (or
     creates) destination warehouse stock. Same two-pass validation pattern as
     SalesOrder. Rejects if fromWarehouse == toWarehouse (validated both via
     Zod `.refine()` on frontend AND `@AssertTrue` custom validator on backend
     DTO — backend is the real security boundary).

**Plus:**
- **user** module — User entity + UserRepository + UserDetailsServiceImpl +
  AuthController (register/login)
- **security** module — JwtUtil, JwtAuthFilter, SecurityConfig
- **common** module — ErrorResponse, GlobalExceptionHandler
- **dashboard** module — `GET /api/dashboard/summary` — aggregate stats:
  totalInventoryValue, totalStockUnits, lowStockItemsCount (threshold: qty < 20),
  lowStockItems list, pendingPurchaseOrders count, pendingSalesOrders count
- **config** module — OpenApiConfig (Swagger metadata + JWT bearer auth button
  in Swagger UI)

## Frontend Architecture

**Routing:** Next.js App Router. Route group `(app)` wraps all authenticated
pages with a shared layout (`src/app/(app)/layout.tsx`) — sidebar nav +
centralized auth-redirect-if-not-logged-in logic. This means individual pages
inside `(app)/` do NOT need to repeat auth-check logic.

**Auth flow:** `AuthContext` (`src/context/AuthContext.tsx`) — React Context
providing `token`, `role`, `login()`, `logout()`, `isLoading`. Token + role
stored in `localStorage` (explicitly flagged as a known tradeoff — httpOnly
cookies would be more XSS-resistant, noted as a future hardening step, not
done yet). `api.ts` (`src/lib/api.ts`) is a shared Axios instance with a
request interceptor that auto-attaches `Authorization: Bearer <token>` from
localStorage to every request.

**Pages built (all under `src/app/(app)/`):**
- `dashboard/page.tsx` — summary cards from `/api/dashboard/summary`
- `warehouses/page.tsx` — list + create form
- `inventory/page.tsx` — stock table (low-stock highlighted red, threshold 20),
  "Adjust" button opens a Dialog with a single number input (+/- change amount)
- `products/page.tsx` — list + create form, Category/Supplier Select dropdowns
- `categories/page.tsx` — list + create form
- `suppliers/page.tsx` — list + create form
- `purchase-orders/page.tsx` — list + create form WITH dynamic line items
  (react-hook-form's `useFieldArray` — add/remove product+quantity rows),
  "Receive" button on PENDING orders
- `sales-orders/page.tsx` — same pattern, "Ship" button
- `transfers/page.tsx` — same pattern, from/to warehouse selects (validated
  different), "Complete" button

**Every list page follows this exact pattern:**
1. Zod schema for the create-form validation
2. react-hook-form's `useForm` with `defaultValues` set for EVERY field
3. `loadX()` function fetching paginated data, storing `.content` and
   `.totalPages` in state
4. `useEffect(() => { loadX() }, [page])` — refetches when page changes
5. Previous/Next pagination buttons (disabled appropriately at boundaries)
6. `onSubmit` posts to the backend, calls `reset({...explicit defaults})`,
   then reloads the list

Login page (`src/app/login/page.tsx`) is OUTSIDE the `(app)` route group
(it's a public page, not wrapped in the authenticated layout).

**shadcn components installed:** button, input, label, card, field, separator,
select, dialog (via `npx shadcn@latest add <name>`)

## Testing (Phase A work)

**Unit tests written (all passing), using Mockito (@Mock/@InjectMocks pattern):**
- `WarehouseServiceTest` — basic CRUD (createWarehouse, getAllWarehouses/paged)
- `InventoryServiceTest` — adjustStock: increase, decrease, reject-negative,
  reject-not-found (4 tests)
- `PurchaseOrderServiceTest` — receivePurchaseOrder: increase existing
  inventory, create new inventory record, reject already-received, verify
  status transition (4 tests)
- `SalesOrderServiceTest` — shipSalesOrder: reduce stock, reject insufficient
  stock (verifies NO partial save happens), reject no-inventory-record, reject
  already-shipped (4 tests)
- `TransferServiceTest` — completeTransfer: move stock between warehouses
  (verifies BOTH the source decrease AND destination increase saves happen),
  reject insufficient source stock, reject already-completed (3 tests)

**Integration test — UNRESOLVED, see Known Quirks #9 and TODO.md:**
- `WarehouseIntegrationTest` (`@SpringBootTest` + `@AutoConfigureMockMvc` +
  H2 in-memory DB via `application-test.properties` + `@ActiveProfiles("test")`)
  — currently disabled with `@Disabled` annotation, not deleted. Meant to test
  the real HTTP → Security → Controller → Service → DB flow (401 unauthenticated,
  200 as ADMIN, 400 validation error, 403 as wrong-role STAFF). All 4 sub-tests
  currently return 403 regardless of expected outcome — suspected CORS filter
  interaction with MockMvc, not yet root-caused.

## Development Workflow / Habits Established

- Backend: IntelliJ IDEA (opened at `SupplyNext/backend`, NOT the monorepo root —
  though this could be reconsidered)
- Frontend: VS Code (opened at `SupplyNext/frontend`)
- Postman for manual API testing (no formalized collection built yet — was
  discussed as a good idea but not actually set up)
- Git: commit after every meaningful working feature, with descriptive messages.
  Both frontend and backend changes in the same repo — commits are made from
  the SupplyNext root.
- Pattern for the person's learning style: they want CODE PROVIDED (copy-paste
  workflow), with explanations of WHY/key concepts alongside — not expected to
  write Spring Boot code independently from scratch, but does want genuine
  understanding of what's happening, and proactively researches things
  themselves sometimes (e.g. found the shadcn Form deprecation info
  independently).
- They explicitly want to be told about tradeoffs/shortcuts taken (e.g. the
  hardcoded JWT secret, localStorage vs httpOnly cookies) rather than have
  these silently glossed over.

## Overall Project Roadmap (agreed with the person)

Phase A — Architecture & Engineering [MOSTLY COMPLETE — see TODO.md]
Phase B — Smart Analytics (EOQ, ABC Analysis, Safety Stock, Reorder Point,
  Dead Stock Detection, Supplier Analytics, KPI Dashboard, Charts) [NEXT UP]
Phase C — AI (Python FastAPI microservice, real demand forecasting) [NOT STARTED]
Phase D — UI/UX Polish (toasts, skeletons, search/filters, advanced tables,
  dark mode, responsive design) [NOT STARTED]
Phase E — Deployment & DevOps (Docker, CI/CD, hosting) [NOT STARTED]
Phase F — Documentation (README, architecture diagram, ER diagram, screenshots,
  demo video) [NOT STARTED]

Reasoning for this order (agreed with the person, worth preserving): smart
analytics (Phase B) should come BEFORE real ML forecasting (Phase C) because
the ML phase depends on clean historical data queries/aggregation patterns
that Phase B builds — building ML first would mean redoing groundwork later.
Polish (Phase D) comes after both feature phases so it's applied to the final
feature set once, not redone repeatedly. Deployment (Phase E) comes after
polish so we deploy something we're proud of, not something rough that needs
immediate re-deployment.
