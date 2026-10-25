package com.fintech.ledger.account;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

// @Digits allows at most 15 digits before the decimal point and 2 after, so 10.001 is rejected
public record DepositRequest(
        @NotNull @Positive @Digits(integer = 15, fraction = 2) BigDecimal amount) {
}
