# ms-banks — API

Endpoints and error codes. Envelope shape, exception hierarchy and the DomainError → HTTP
mapping: parent `.ai/references/APP_STRUCTURE.md` — not repeated here.

## Endpoints

| Method | Path | Purpose | Error codes |
|---|---|---|---|
| GET | `/api/v1/banks` | List the user's banks, each with their account list | — |
| GET | `/api/v1/banks/{bankNumber}` | Get one bank with the user's accounts at that bank | `resource_not_found` |
| GET | `/api/v1/banks/accounts` | List user accounts; filter by type, currency, bankNumber, name, hideEmpty | — |
| POST | `/api/v1/banks/accounts` | Open an account under a bank | `resource_already_exists`, `invalid_cbu`, `cbu_bank_mismatch`, `invalid_account_number`, `invalid_currency` |
| GET | `/api/v1/banks/accounts/{cbu}` | Get one account by CBU | `resource_not_found`, `invalid_cbu` |
| PATCH | `/api/v1/banks/accounts/{cbu}` | Update account name, balance, or active flag | `resource_not_found`, `resource_already_exists` |
| DELETE | `/api/v1/banks/accounts/{cbu}` | Close (delete) an account | `resource_not_found`, `account_not_deletable` |
| PUT | `/api/v1/banks/accounts/{cbu}/fees` | Upsert an account's fee schedule | `resource_not_found`, `invalid_fee_schedule` |
| GET | `/api/v1/banks/accounts/{cbu}/transactions` | Account transaction history; `?all=true` or `?from=&to=`, proxied from ms-finances | `resource_not_found`, `invalid_date_range`, `finances_service_unavailable` |
| POST | `/api/v1/banks/accounts/{cbu}/balance/adjust` | Manually adjust balance by a signed delta | `resource_not_found`, `account_insufficient_funds`, `account_currency_mismatch` |
| GET | `/api/v1/banks/available` | Read-only catalog of all bank names and numbers | — |
| GET | `/api/v1/banks/balance-snapshots?from=&to=` | Daily balance snapshot history by currency | `invalid_date_range` |
| GET | `/api/v1/banks/cards` | List user cards; optional `?bankNumber=` | — |
| POST | `/api/v1/banks/cards` | Issue a card (optional `creditLimit`) | `resource_not_found`, `resource_already_exists`, `invalid_card_number`, `invalid_card_check_digit`, `invalid_issuer_bin`, `invalid_issuer_card_account`, `card_invalid_type` |
| GET | `/api/v1/banks/cards/{cardNumber}` | Get one card by 16-digit number | `resource_not_found` |
| PATCH | `/api/v1/banks/cards/{cardNumber}` | Update billing cycle, expiry date, or `creditLimit` | `resource_not_found`, `card_expired`, `card_invalid_type` |
| DELETE | `/api/v1/banks/cards/{cardNumber}` | Cancel (delete) a card | `resource_not_found`, `card_not_deletable` |
| PUT | `/api/v1/banks/cards/{cardNumber}/fees` | Upsert a card's fee schedule | `resource_not_found`, `invalid_fee_schedule` |
| GET | `/api/v1/banks/cards/{cardNumber}/installments` | List all installments for a card | `resource_not_found` |
| POST | `/api/v1/banks/cards/{cardNumber}/installments` | Register a card expense and generate its schedule | `resource_not_found`, `card_installment_not_supported`, `card_expired` |
| POST | `/api/v1/banks/cards/{cardNumber}/installments/{installmentId}/pay` | Mark an installment paid from an account CBU | `resource_not_found`, `card_installment_already_paid`, `card_installment_mismatch`, `account_insufficient_funds`, `account_currency_mismatch` |
| POST | `/api/v1/banks/cards/{cardNumber}/installments/duplicates-check` | Pre-flight: indices of expenses that already exist | `resource_not_found` |
| POST | `/api/v1/banks/cards/{cardNumber}/installments/import` | Batch import expenses from a statement (skips duplicates) | `resource_not_found`, `card_installment_not_supported` |
| GET | `/api/v1/banks/fees` | All account and card fee schedules for the user, with computed tax rates | — |
| GET | `/api/v1/banks/loans` | List user loans; optional `?bankNumber=` | — |
| POST | `/api/v1/banks/loans` | Originate a loan (French amortisation, full schedule) | `resource_not_found`, `loan_account_mismatch`, `invalid_currency` |
| DELETE | `/api/v1/banks/loans/{id}` | Cancel (delete) a loan | `resource_not_found`, `loan_already_closed` |
| GET | `/api/v1/banks/loans/{id}/installments` | List installments for a loan | `resource_not_found` |
| POST | `/api/v1/banks/loans/{id}/installments/{installmentId}/pay` | Mark a loan installment paid from an account CBU | `resource_not_found`, `loan_already_closed`, `loan_installment_already_paid`, `loan_installment_mismatch`, `account_insufficient_funds`, `account_currency_mismatch` |
| GET | `/api/v1/banks/metadata` | Enum catalog (account types, card brands/types/behaviors) for form selectors | — |
| GET | `/api/v1/banks/upcoming-payments?from=&to=` | Consolidated upcoming unpaid loan and card installments | — |

## DomainError catalog

| Slug | HTTP status | When it is thrown |
|---|---|---|
| `resource_not_found` | 404 | Lookup found nothing for the given id/CBU/card number |
| `resource_already_exists` | 409 | Unique constraint would be violated on create |
| `bank_has_active_accounts` | 409 | Deleting a bank blocked by accounts still open on it |
| `account_not_deletable` | 409 | Closing an account blocked by dependent data |
| `card_not_deletable` | 409 | Cancelling a card blocked by dependent data |
| `account_insufficient_funds` | 422 | Debit exceeds the account's balance |
| `account_currency_mismatch` | 422 | Adjustment currency differs from the account's currency |
| `card_expired` | 422 | Operation attempted on a card past its `expiringDate` |
| `card_installment_already_paid` | 409 | `pay()` called on an installment already marked paid |
| `card_installment_mismatch` | 422 | Installment does not belong to the card in the path |
| `card_installment_not_supported` | 422 | Installment op on a card whose behavior forbids it (`DebitCard`) |
| `card_invalid_type` | 422 | Card-type-specific operation on the wrong subtype |
| `loan_already_closed` | 409 | Op on a loan that is no longer `active` |
| `loan_account_mismatch` | 422 | Deposit account does not belong to the loan's user |
| `loan_installment_already_paid` | 409 | `pay()` called on a loan installment already paid |
| `loan_installment_mismatch` | 422 | Installment does not belong to the loan in the path |
| `invalid_date_range` | 400 | `from` is after `to` in a date-filtered query |
| `unsupported_bank` | 400 | `bankNumber` not in the seeded catalog |
| `invalid_currency` | 400 | Currency code not a valid ISO 4217 code |
| `invalid_card_number` | 400 | PAN is not 15 or 16 digits |
| `invalid_card_check_digit` | 400 | 16-digit PAN's Luhn digit does not match |
| `invalid_issuer_bin` | 400 | BIN segment is not 6 digits |
| `invalid_issuer_card_account` | 400 | Issuer-account segment is not 9 digits |
| `invalid_bank_number` | 400 | Bank number is not a 3-digit code |
| `invalid_sucursal_code` | 400 | Sucursal code is not a 4-digit code |
| `invalid_account_number` | 400 | Account number is not 13 digits |
| `invalid_cbu` | 400 | CBU is not 22 digits, or a check digit fails |
| `cbu_bank_mismatch` | 422 | CBU's leading 3 digits don't match the target bank |
| `invalid_balance_snapshot` | 400 | Snapshot construction invariant violated |
| `invalid_card_billing` | 400 | `closingDay`/`dueDay` outside `[1, 31]` |
| `invalid_fee_schedule` | 400 | Fee-schedule invariant violated (e.g. surcharge % out of range) |
| `finances_service_unavailable` | 500 | The ms-finances Feign call failed |
| `internal_error` | 500 | Unmapped failure |
