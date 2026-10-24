# payments-ledger-service

Learning project: a payments and ledger service built step by step (Spring Boot, Java 17).

## Run
- IntelliJ: open this folder, wait for Maven to finish, then run `LedgerApplication`.
- Check: open http://localhost:8080/api/health

## Test
- IntelliJ: right-click `src/test/java` -> Run 'All Tests'

## Roadmap
1. Accounts and transfers REST API
2. PostgreSQL, double-entry ledger, paged history
3. Idempotency keys, concurrency safety
4. Fraud rules engine, statements
5. Async notifications, Kafka, retries
6. Tests, Docker, AWS deployment
