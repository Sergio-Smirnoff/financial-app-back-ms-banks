# ms-banks

Banking-instruments microservice for the financial-app platform. **This is its own git repo** (`financial-app-back-ms-banks`) — commit banks work here, not the parent repo.

Owns banks, accounts (CBU-addressed), credit/debit cards with installment schedules, loans amortised via the French method, upcoming-payments aggregation, and metadata for form selectors. Domain events published via a transactional outbox (CloudEvents) on balance adjustments, low balance, installment payments, and the daily card/loan alerts. Consumes `finances.transaction.created` from ms-finances to keep account balances in sync.

**Port:** 8083 | **Schema:** `banks` | **Stack:** Java 21, Spring Boot 3.4.2, JPA, Flyway, MapStruct, Kafka

> Full design: `docs/specs/services/ms-banks.md` (parent workspace).

---

## Run

```bash
# Via dev script (recommended — starts infra automatically)
./scripts/dev.sh local service-banks

# Direct Maven (infra must already be running)
cd back/ms-banks
mvn spring-boot:run

# Tests
mvn test       # unit tests
mvn verify     # unit + integration tests
```

Swagger UI: http://localhost:8083/swagger-ui.html

---

## Domain

| Aggregate | Key VOs | Notes |
|-----------|---------|-------|
| `Bank` | `BankNumber` (3-digit BCRA code), `Logo` | Read-only catalog seeded at startup |
| `Account` | `Cbu` (22-digit), `AccountNumber`, `SucursalCode`, `Money`, `UserId` | Subtypes: `CheckingAccount`, `SavingsAccount`, `InvestmentAccount` |
| `Card` | `CardNumber` (16-digit Luhn), `CardDetails` (`CardBrand`, `CardType`, `CardBehavior`, `YearMonth`, `CardBilling`) | Subtypes: `CreditCard`, `DebitCard` |
| `CardInstallment` | `CardInstallmentId`, `Money` | Immutable record; `pay()` returns new instance |
| `Loan` | `LoanId`, `BankNumber`, `Money`, `AmortizationType` | Record; `originate()` builds full French-method schedule |
| `LoanInstallment` | `LoanInstallmentId`, `Money` | Immutable record; `pay()` returns new instance |

`INVESTMENT` accounts are metadata-only — the aggregate throws `AccountInvestmentRestrictionException` on any balance adjustment attempt.

**CBU contract:** `BankNumber` (3-digit BCRA code) prefixes every CBU. `Cbu.from(String)` validates both BCRA modulo-10 check digits. All API paths and Kafka payloads address accounts by CBU string.

---

## Endpoints

All endpoints return the shared envelope `{ status, title, code, message, data }` defined in
`commons-core` (built from `financial-app-parent`). `status`/`title` mirror the HTTP status;
`code` appears only on errors and carries the machine-readable `DomainError` slug
(e.g. `account_insufficient_funds`); error details (validation field map, constraint info)
travel in `data`. Errors are rendered by `GlobalExceptionHandler extends ApiExceptionHandler`
(commons-web), and every endpoint declares its throwable codes with `@ApiErrorCodes`, so the
Swagger UI lists each error code with a generated example body.

### BankController — `/api/v1/banks`

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/banks` | List the user's banks (those where they hold accounts), each with their account list |
| `GET` | `/api/v1/banks/available` | Read-only catalog of all available bank names and numbers |
| `GET` | `/api/v1/banks/{bankNumber}` | Get one bank with the user's accounts at that bank |

### AccountController — `/api/v1/banks/accounts`

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/banks/accounts` | List user accounts; filterable by `type`, `currency`, `bankNumber`, `name`, `hideEmpty` |
| `GET` | `/api/v1/banks/accounts/{cbu}` | Get one account by CBU |
| `POST` | `/api/v1/banks/accounts` | Open (create) an account under a bank |
| `PATCH` | `/api/v1/banks/accounts/{cbu}` | Update account name, balance, or active flag |
| `DELETE` | `/api/v1/banks/accounts/{cbu}` | Close (delete) an account |
| `GET` | `/api/v1/banks/accounts/{cbu}/transactions` | Account transaction history; `?all=true` or `?from=&to=`; default last 5 (proxied from ms-finances) |
| `POST` | `/api/v1/banks/accounts/{cbu}/balance/adjust` | Manually adjust balance by a signed delta |

### CardController — `/api/v1/banks/cards`

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/banks/cards` | List user cards; optional `?bankNumber=` filter |
| `GET` | `/api/v1/banks/cards/{cardNumber}` | Get one card by 16-digit card number |
| `POST` | `/api/v1/banks/cards` | Issue (create) a card |
| `PATCH` | `/api/v1/banks/cards/{cardNumber}` | Update billing cycle (closingDay, dueDay) and expiry date |
| `DELETE` | `/api/v1/banks/cards/{cardNumber}` | Cancel (delete) a card |

### CardInstallmentController — `/api/v1/banks/cards/{cardNumber}/installments`

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/banks/cards/{cardNumber}/installments` | List all installments for a card |
| `POST` | `/api/v1/banks/cards/{cardNumber}/installments` | Register a new card expense and generate its installment schedule |
| `POST` | `/api/v1/banks/cards/{cardNumber}/installments/{installmentId}/pay` | Mark one installment as paid from a given account CBU |
| `POST` | `/api/v1/banks/cards/{cardNumber}/installments/import` | Batch import card expenses from a statement (skips duplicates) |
| `POST` | `/api/v1/banks/cards/{cardNumber}/installments/duplicates-check` | Pre-flight check: returns indices of expenses that already exist |

### LoanController — `/api/v1/banks/loans`

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/banks/loans` | List user loans; optional `?bankNumber=` filter |
| `POST` | `/api/v1/banks/loans` | Originate a loan (French amortisation, full schedule generated) |
| `DELETE` | `/api/v1/banks/loans/{id}` | Cancel (delete) a loan |
| `GET` | `/api/v1/banks/loans/{id}/installments` | List installments for a loan |
| `POST` | `/api/v1/banks/loans/{id}/installments/{installmentId}/pay` | Mark one loan installment as paid from a given account CBU |

### MetadataController — `/api/v1/banks/metadata`

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/banks/metadata` | Read-only catalog of valid enum values for account types, card brands, card types, and card behaviors |

### UpcomingPaymentController — `/api/v1/banks/upcoming-payments`

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/api/v1/banks/upcoming-payments?from=&to=` | Consolidated list of upcoming unpaid loan and card installments within a date range |

---

## Source layout

```
src/main/java/com/financialapp/banks/
│
├── web/
│   ├── controller/
│   │   ├── AccountController
│   │   ├── BankController
│   │   ├── CardController
│   │   ├── CardInstallmentController
│   │   ├── LoanController
│   │   ├── MetadataController
│   │   └── UpcomingPaymentController
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── mapper/
│   └── error/                     (GlobalExceptionHandler)
│
├── application/
│   ├── account/impl/              (OpenAccount, CloseAccount, UpdateAccount,
│   │                               AdjustBalance, ListAccounts, GetAccount,
│   │                               GetAccountTransactions)
│   ├── bank/impl/                 (ListBanks, ListAvailableBanks, GetBank)
│   ├── card/impl/                 (IssueCard, CancelCard, UpdateCard, GetCard,
│   │                               ListCards, RegisterCardExpense,
│   │                               ListCardInstallments, PayCardInstallment,
│   │                               ImportCardExpenses, CheckDuplicateExpenses)
│   ├── catalog/impl/              (GetBankingCatalog)
│   ├── loan/impl/                 (OriginateLoan, ListLoans, GetLoanInstallments,
│   │                               PayLoanInstallment, CancelLoan)
│   └── upcoming/impl/             (GetUpcomingPayments)
│
├── domain/                        (no Spring dependencies)
│   ├── common/model/              (Cbu, Money, UserId, Installment, Payment)
│   ├── model/
│   │   ├── account/               (Account, AccountType, AccountNumber, AccountAdjustment)
│   │   ├── bank/                  (Bank, BankNumber, Logo, SucursalCode)
│   │   ├── card/                  (Card, CardInstallment, CardDetails, CardBrand,
│   │   │                           CardType, CardBehavior, CardBilling, CardNumber)
│   │   └── loan/                  (Loan, LoanInstallment, LoanId, LoanOrigination,
│   │                               AmortizationType)
│   ├── usecase/                   (use-case interfaces + command records)
│   ├── repository/
│   ├── port/                      (DomainEventPublisher, FinancesPort, InvestmentsPort)
│   ├── service/                   (LoanAmortization, CardInstallmentEventFactory)
│   ├── event/                     (BalanceAdjustedEvent, LowBalanceEvent,
│   │                               LoanCreatedEvent, LoanInstallmentPaidEvent,
│   │                               CardInstallmentPaidEvent)
│   └── exception/                 (DomainException, DomainError, ErrorCategory,
│                                   account/*, bank/*, card/*, cbu/*, loan/*)
│
└── infrastructure/
    ├── persistence/
    │   ├── entity/                (AccountJpaEntity, BankJpaEntity, CardJpaEntity,
    │   │                           CardInstallmentJpaEntity, LoanJpaEntity,
    │   │                           LoanInstallmentJpaEntity, ProcessedEventJpaEntity)
    │   ├── jpa/                   (Spring Data JPA repositories)
    │   ├── mapper/
    │   ├── repository/            (domain port implementations)
    │   ├── query/                 (UpcomingInstallmentsQueryAdapter)
    │   └── seed/                  (BankCatalogSeeder)
    ├── messaging/
    │   ├── KafkaDomainEventPublisher
    │   ├── listener/              (TransactionEventListener)
    │   ├── payload/               (TransactionCreatedEvent, BankAlertEvent, …)
    │   └── mapper/
    ├── client/
    │   ├── FinancesFeignClient
    │   ├── InvestmentsFeignClient
    │   ├── adapter/               (FinancesClientAdapter, InvestmentsClientAdapter)
    │   └── dto/                   (ExternalApiResponse)
    ├── scheduler/                 (BankAlertScheduler)
    └── config/                    (JPA, Kafka, Feign, serializers)
```

---

## Kafka

CloudEvents 1.0 (Kafka binding, binary mode) via `commons-messaging`. Outbound events use a transactional outbox (`outbox_event`) + commons `OutboxRelay`; inbound is deduped on `ce_id` (`processed_events`). Topic `= ce_type`.

| Direction | Topic | Trigger |
|-----------|-------|---------|
| Consumes | `finances.transaction.created` | ms-finances records a transaction; ms-banks adjusts the addressed account balance (idempotent via `processed_events`) |
| Publishes → ms-finances | `banks.payment.recorded` | After a loan/card installment is paid — records the cash leg in ms-finances |
| Publishes → ms-notifications | `banks.account.balance_adjusted` | After every successful credit or debit |
| Publishes → ms-notifications | `banks.account.low_balance` | Post-adjustment balance < 500 |
| Publishes → ms-notifications | `banks.loan.reminder` | Daily 08:00 scheduler — loan installments due ≤3d |
| Publishes → ms-notifications | `banks.card.expiring` | Daily 08:00 scheduler — cards expiring ≤30d |
| Publishes → ms-notifications | `banks.card.installment_due` | Daily 08:00 scheduler — card installments due ≤3d |

Delivery is at-least-once via outbox + relay (no `AFTER_COMMIT`); failed consumes retry then land on `<topic>.DLT`.

---

## Flyway migrations

| Version | Description |
|---------|-------------|
| V1 | Create `banks` and `accounts` tables |
| V2 | Create `cards` and `card_installments` tables |
| V3 | Create `loans` and `loan_installments` tables |
| V4 | Migrate cards and loans to bank-level (add `bank_id`) |
| V5 | Wipe cards and loans data (structural reset) |
| V6 | Create `processed_events` table (idempotency) |
| V7 | Add `cbu` and `alias` columns to `accounts` |
| V8 | Add `card_number` column to `cards` |
| V9 | Make `banks` a global read-only catalog |
| V10 | Drop `logo_url` from bank catalog |
| V11 | Drop `last_4_digits` from cards |
| V12 | Normalise legacy `card_behavior` values |
| V13 | Add `bank_number` column to `banks` |
| V14 | Change `bank_number` to `varchar` |

---

## Required environment variables

| Variable | Purpose |
|---|---|
| `DB_URL` | PostgreSQL JDBC URL — e.g. `jdbc:postgresql://postgres:5432/financialapp?currentSchema=banks` |
| `DB_USERNAME` | Database user |
| `DB_PASSWORD` | Database password |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker — e.g. `kafka:9092` |
| `INTERNAL_AUTH_TOKEN` | Shared secret for `X-Internal-Token` header; service hard-fails at startup without it |

Copy `.env.example` (workspace root) to `.env` in this directory and fill in the values.

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` | PRs; push to develop/master | tests + docker build via shared `backend-ci.yml` |
| `docker-publish.yml` | push to master; `v*` tags | GHCR publish: `latest`, `sha-*`, semver on tags |
| `release.yml` | manual (bump dropdown) | next `vX.Y.Z` tag + Release + versioned publish |

Reusable workflows live in the root repo `Sergio-Smirnoff/financial-app`.
