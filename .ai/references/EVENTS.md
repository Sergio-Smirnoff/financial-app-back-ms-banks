# ms-banks — messaging and jobs

CloudEvents 1.0, Kafka binary mode, via `commons-messaging`. Topic name = `ce_type`. Outbox,
`OutboxRelay` and DLT conventions: parent `.ai/references/ARCHITECTURE.md` — not repeated here.

## Published

| ce_type / topic | when emitted | payload fields |
|---|---|---|
| `banks.payment.recorded` | loan originated (deposits principal), loan installment paid, or card installment paid — records the cash leg in ms-finances | userId, accountCbu, amount, currency, description, date |
| `banks.account.balance_adjusted` | after every successful credit or debit | userId, accountName, accountCbu, bankNumber, amount, currency, credit |
| `banks.account.low_balance` | post-adjustment balance < 500 in the account's own currency | userId, accountName, accountCbu, bankNumber, balance, currency |
| `banks.loan.reminder` | `BankAlertScheduler` — loan installment due ≤3 days | userId, loanId, installmentId, installmentNumber, loanName, dueDate |
| `banks.card.expiring` | `BankAlertScheduler` — card expiring ≤30 days | userId, cardNumber, bankNumber, expiringDate |
| `banks.card.installment_due` | `BankAlertScheduler` — card installment due ≤3 days | userId, cardNumber, installmentId, installmentNumber, totalInstallments, description, dueDate, amount, currency |

## Consumed

| ce_type | handler | idempotency key | DLT behaviour |
|---|---|---|---|
| `finances.transaction.created` | `TransactionEventListener.handleTransactionCreated` — adjusts the addressed account's balance | `ce_id`, via `inbound_events` (`ProcessedEventGateway`) | retries, then lands on `finances.transaction.created.DLT` |

## Scheduled jobs

| Job | Cron | What it does |
|---|---|---|
| `BankAlertScheduler.runDailyAlerts` | `0 0 8 * * *` | Card expirations ≤30d, upcoming loan/card installments ≤3d, low-balance accounts <500 — writes the 4 alert topics above to the outbox; a failure rolls back the whole run |
| `BalanceSnapshotScheduler.captureDailySnapshots` | `0 0 0 * * *` | Captures one `BalanceSnapshot` per user (cash, current-statement unpaid card installments, active loan principal), with per-user error isolation |

## Outbound calls

| Target service | Endpoint | Why |
|---|---|---|
| ms-finances | `GET /api/v1/finances/transactions` (`FinancesFeignClient`) | Proxied by `GET /accounts/{cbu}/transactions` |
