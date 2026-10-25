package com.fintech.ledger.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final String ownerName;
    private final String currency;
    private BigDecimal balance;
    private final Instant createdAt;

    public Account(UUID id, String ownerName, String currency, BigDecimal balance, Instant createdAt) {
        this.id = id;
        this.ownerName = ownerName;
        this.currency = currency;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    // A new account always starts at zero. Balances will come from ledger entries, never be set directly.
    // The String constructor keeps the exact scale (0.00); new BigDecimal(0.0) would go through a double.
    public static Account open(String ownerName, String currency) {
        return new Account(UUID.randomUUID(), ownerName, currency, new BigDecimal("0.00"), Instant.now());
    }

    // The domain guards its own rule even though the request is validated: other callers may not be.
    // Not thread-safe yet: two deposits at the same moment can lose one (lost update). Fixed in step 3.
    public void deposit(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive, got " + amount);
        }
        // BigDecimal is immutable: add() returns a new value, so it must be assigned back
        balance = balance.add(amount);
    }

    public UUID getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
