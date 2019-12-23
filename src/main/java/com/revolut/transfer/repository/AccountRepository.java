package com.revolut.transfer.repository;

import com.revolut.transfer.model.Account;

import java.util.Collection;
import java.util.UUID;

public interface AccountRepository {

    void save(final Account account);

    void saveAll(final Collection<Account> accounts);

    Account getAccountById(UUID accountId);

    Collection<Account> getAllAccounts();

    void deleteAccount(UUID accountId);

    void deleteAllAccounts();

    boolean isUserExists(String userId);
}
