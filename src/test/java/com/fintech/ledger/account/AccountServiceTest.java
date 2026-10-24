package com.fintech.ledger.account;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Plain unit test: no Spring context, just the service wired to the real in-memory repository.
class AccountServiceTest {

    private final AccountService accountService = new AccountService(new InMemoryAccountRepository());

    @Test
    void createOpensAccountWithZeroBalance() {
        Account account = accountService.create("Alice", "INR");

        assertThat(account.getId()).isNotNull();
        assertThat(account.getOwnerName()).isEqualTo("Alice");
        assertThat(account.getCurrency()).isEqualTo("INR");
        // compareTo-based: BigDecimal.equals would treat 0 and 0.00 as different
        assertThat(account.getBalance()).isEqualByComparingTo("0");
        assertThat(account.getCreatedAt()).isNotNull();
    }

    @Test
    void createGivesEachAccountAUniqueId() {
        Account first = accountService.create("Alice", "INR");
        Account second = accountService.create("Alice", "INR");

        assertThat(first.getId()).isNotEqualTo(second.getId());
    }

    @Test
    void getReturnsCreatedAccount() {
        Account created = accountService.create("Alice", "INR");

        assertThat(accountService.get(created.getId())).isSameAs(created);
    }

    @Test
    void getThrowsWhenAccountDoesNotExist() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> accountService.get(unknownId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }
}
