package com.fintech.ledger.account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// What the API returns. Kept separate from Account so the JSON shape and the domain model can change independently.
public record AccountResponse(UUID id, String ownerName, String currency, BigDecimal balance, Instant createdAt) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwnerName(),
                account.getCurrency(),
                account.getBalance(),
                account.getCreatedAt());
    }
}
