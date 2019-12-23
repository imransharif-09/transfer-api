package com.revolut.transfer.repository;

import com.google.inject.Inject;
import com.revolut.transfer.model.Account;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AccountRepositoryImpl implements AccountRepository {

    private final Map<UUID, Account> accountsStorage;

    @Inject
    public AccountRepositoryImpl() {
        this.accountsStorage = new ConcurrentHashMap<>();
    }

    @Override
    public void save(Account account) {
        accountsStorage.put(account.getId(), account);
    }

    @Override
    public void saveAll(Collection<Account> accounts) {
        accounts.forEach(account -> accountsStorage.put(account.getId(), account));
    }

    @Override
    public Account getAccountById(UUID id) {
        return accountsStorage.get(id);
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountsStorage.values().stream().collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void deleteAccount(UUID accountId) {
        accountsStorage.remove(accountId);
    }

    @Override
    public void deleteAllAccounts() {
        accountsStorage.clear();
    }

    @Override
    public boolean isUserExists(String userId) {
        return accountsStorage.values().stream().anyMatch(account -> account.getUserId().equals(userId));
    }
}
