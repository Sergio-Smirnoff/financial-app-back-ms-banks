# ms-banks

Bank accounts, cards, loans and their installments for the financial-app platform.
Port **8083**, schema **`banks`**. Own git repo — commit banks work here, never from the
parent workspace.

## Package tree

com.financialapp.banks
├── domain            pure — aggregates, VOs, ports, domain services. No Spring.
├── application       use-case implementations, @Transactional lives here
├── web               controllers, DTOs, MapStruct mappers
└── infrastructure    JPA adapters, Kafka, Feign, scheduler

Layer boundaries are enforced by `LayeredArchitectureTest` (ArchUnit) — a violation fails
`mvn verify`, it is not a review opinion.

## Load-bearing facts

- Only aggregate roots have repositories — seven of them; `DOMAIN.md` has the table.
  Installments are reached through their root, never directly.
- `BankNumber` is the 3-digit BCRA entity code that prefixes every `Cbu`. `Cbu` validates both
  modulo-10 check digits on construction.
- ms-banks holds `CHECKING` and `SAVINGS` accounts only. There is no `INVESTMENT` type here —
  an investment account is a derived read-model in ms-investments keyed by `BankNumber`.
- Outbound events go through the transactional outbox (`outbox_event`), published by the
  commons `OutboxRelay`. Never `AFTER_COMMIT`, never a direct send from a use case.
- Inbound consumption is idempotent via `banks.inbound_events` (V16), reached through
  `ProcessedEventGateway`. The V6 `processed_events` table is dead — no entity maps it.

## Read when

| File | Read when |
|---|---|
| `.ai/references/DOMAIN.md` | changing an aggregate, a value object, an enum or a migration |
| `.ai/references/API.md` | adding or changing an endpoint, a DTO or an error code |
| `.ai/references/EVENTS.md` | touching Kafka, the outbox, a scheduled job or a Feign call |

## Global rules

R1–R18, the four workflow modes, the tech stack, the response envelope and the exception
hierarchy live in the **parent workspace** at `.ai/references/`. They are not duplicated here.
Working in this repo without the parent workspace present means working without the rules —
open `financial-app/` as the project root.

Human onboarding and how to run this service: `README.md`.
