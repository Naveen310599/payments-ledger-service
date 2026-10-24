package com.fintech.ledger.account;

import com.fintech.ledger.common.NotFoundException;

import java.util.UUID;

public class AccountNotFoundException extends NotFoundException {

    public AccountNotFoundException(UUID id) {
        super("Account", id);
    }
}
