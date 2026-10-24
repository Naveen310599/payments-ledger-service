package com.fintech.ledger.account;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    // Constructor injection: the dependency is final, and tests can pass in any implementation without Spring.
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account create(String ownerName, String currency) {
        return accountRepository.save(Account.open(ownerName, currency));
    }

    public Account get(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
