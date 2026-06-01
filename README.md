# ms-banks

Banking-instruments microservice for the financial-app platform. Owns banks, accounts,
cards, loans, and their installments. Built with Spring Boot 3.4.2 / Java 21, following a
Domain-Driven Design hexagonal architecture (`domain` → `application` → `web`/`infrastructure`).

## Build & test

```bash
mvn test      # unit tests (Surefire)
mvn verify    # unit + integration tests (*IT via Failsafe)
```

## Context map (integration)

ms-banks is the banking-instruments bounded context. It owns `Bank`, `Account`, `Card`,
`Loan`, and their installments. ms-finances is the transaction ledger; ms-investments is
portfolio valuation.

- **Outbound (downstream) — Anti-Corruption Layer.** `FinancesPort` and `InvestmentsPort`
  (domain) are implemented by Feign adapters in `infrastructure/client/adapter`. Foreign
  responses are deserialized into `infrastructure/client/dto/ExternalApiResponse` (an ACL
  envelope) and mapped to domain types; the web layer's `ApiResponse` never crosses into
  infrastructure.
- **Inbound events.** Kafka `transaction.created` is consumed by `TransactionEventListener`
  and applied via `AdjustBalance` (eventual balance update).
- **Outbound events.** Loan/card/balance domain events are published to Kafka for the
  notifications context.

## Architecture rules

Layer boundaries (web → application → domain; infrastructure → application/domain; domain
depends on nothing) are enforced by `LayeredArchitectureTest` (ArchUnit). `Loan` and `Card`
are aggregate roots that own their installments; only aggregate roots have repositories.
