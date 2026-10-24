package com.fintech.ledger.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Account {

    private final UUID id;
    private final String ownerName;
    private final String currency;
    private final BigDecimal balance;
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
