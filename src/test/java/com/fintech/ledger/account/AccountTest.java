package com.fintech.ledger.account;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Tests the domain rule directly, with no web layer in front to filter bad input.
class AccountTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"0", "-0.01"})
    void depositRejectsMissingOrNonPositiveAmount(BigDecimal amount) {
        Account account = Account.open("Alice", "INR");

        assertThatThrownBy(() -> account.deposit(amount))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("0");
    }
}
