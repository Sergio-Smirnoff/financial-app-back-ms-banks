# ms-banks

Banks microservice — bank accounts, CBU-addressed balances, cards, loans, and installments.

- **Port:** 8083
- **DB schema:** `banks`
- **Tech stack:** Java 21, Spring Boot 3.4.2, Spring MVC, JPA/Hibernate, Flyway, Kafka, MapStruct, Lombok

> Full design: `docs/specs/services/ms-banks.md` (parent workspace).

## Run

```bash
# Via dev script (infra + service, hot-reload)
./scripts/dev.sh local service-banks

# Direct Maven
cd back/ms-banks
mvn spring-boot:run

# Tests
mvn test
```

Swagger UI: http://localhost:8083/swagger-ui.html

## Build

```bash
mvn clean package -DskipTests
```

## CI/CD

| Workflow | Trigger | Does |
|---|---|---|
| `ci.yml` | PRs; push to develop/master | tests + docker build via shared `backend-ci.yml` |
| `docker-publish.yml` | push to master; `v*` tags | GHCR publish: `latest`, `sha-*`, semver on tags |
| `release.yml` | manual (bump dropdown) | next `vX.Y.Z` tag + Release + versioned publish |

Reusable workflows live in the root repo `Sergio-Smirnoff/financial-app`.
