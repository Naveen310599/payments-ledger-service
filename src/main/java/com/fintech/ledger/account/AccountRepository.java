package com.fintech.ledger.account;

import java.util.Optional;
import java.util.UUID;

// Same method signatures as Spring Data's CrudRepository, so step 2 can swap in a JPA version without touching the service.
public interface AccountRepository {

    Account save(Account account);

    Optional<Account> findById(UUID id);
}
