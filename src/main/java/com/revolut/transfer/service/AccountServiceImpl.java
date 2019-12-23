package com.revolut.transfer.service;

import com.google.inject.Inject;
import com.revolut.transfer.converter.AccountResponseConverter;
import com.revolut.transfer.execption.AccountAlreadyExistsException;
import com.revolut.transfer.execption.AccountNotFoundException;
import com.revolut.transfer.execption.InsufficientBalanceException;
import com.revolut.transfer.model.Account;
import com.revolut.transfer.repository.AccountRepository;
import com.revolut.transfer.request.CreateAccountRequest;
import com.revolut.transfer.request.DepositMoneyRequest;
import com.revolut.transfer.request.TransferMoneyRequest;
import com.revolut.transfer.request.WithdrawMoneyRequest;
import com.revolut.transfer.response.AccountResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountResponseConverter accountResponseConverter;

    /**
     *
     * @param accountRepository
     * @param accountResponseConverter
     */
    @Inject
    public AccountServiceImpl(AccountRepository accountRepository, AccountResponseConverter accountResponseConverter) {
        this.accountRepository = accountRepository;
        this.accountResponseConverter = accountResponseConverter;
    }

    @Override
    public UUID createAccount(CreateAccountRequest createAccountRequest) {
        Account account = null;
        if (accountRepository.isUserExists(createAccountRequest.getUserId())) {
            throw new AccountAlreadyExistsException("Account already exists for userId: " + createAccountRequest.getUserId());
        }
        account = createAccountFrom(createAccountRequest);
        accountRepository.save(account);

        return account.getId();
    }

    @Override
    public AccountResponse getAccount(UUID accountId) {
        AccountResponse accountResponse = null;
        synchronized (accountId.toString().intern()) {
            Account account = getAccountFromRepository(accountId);
            accountResponse = accountResponseConverter.createFrom(account);
        }
        return accountResponse;
    }

    @Override
    public void deleteAllAccounts() {
        accountRepository.deleteAllAccounts();
    }



    /*
    * == NOTE ==:
    * Nested locking has been used in this method to synchronize the transfer between accounts.
    * To Avoid DEAD LOCKS to happen,
    * First, I calculate the larger account from two. First lock will be on the larger account, nested lock will be on other accounts.
    * In this case, deadlock will never occur. e.g There are three threads A, B, C where account Number of A > B and B > C.
    * In case of Transaction between A and B, First A will obtain the lock and then B.
    * In case of Transaction between B and C, when B will try to obtain the lock, its already locked.
    *
    * Second important thing to consider is, Lock is obtained on Account ID.
    * Using this approach, Only that account will be locked on which transfer is being made.
    * All other accounts are available for reading the balance.
    * We have also made read synchronized, because we don't want the system to return the wrong balance for the account on which a transfer is being made.
    *
    * */
    @Override
    public void transferMoney(TransferMoneyRequest transferMoneyRequest) {

        this.validateRequest(transferMoneyRequest);

        UUID largerAccountID = findLargerAccountId(transferMoneyRequest.getFromAccount(), transferMoneyRequest.getToAccount());
        UUID smallerAccountId = findSmallerAccountId(transferMoneyRequest.getFromAccount(), transferMoneyRequest.getToAccount());

        /*
        * Nested locking will make sure to avoid deadlocks. Also taking locks on account id will not lock other requests for other accounts.
        * */
        synchronized (largerAccountID.toString().intern()) {
            synchronized (smallerAccountId.toString().intern()) {
                if (transferMoneyRequest.getAmount().compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Invalid transfer amount provided. Transfer amount should be positive.");
                }

                Account fromAccount = getAccountFromRepository(transferMoneyRequest.getFromAccount());
                Account toAccount = getAccountFromRepository(transferMoneyRequest.getToAccount());

                if (fromAccount.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                    throw new InsufficientBalanceException("Insufficient balance in account id: " + transferMoneyRequest.getFromAccount());
                }

                if (fromAccount.getBalance().compareTo(transferMoneyRequest.getAmount()) < 0) {
                    throw new InsufficientBalanceException("Insufficient balance in account id: " + transferMoneyRequest.getFromAccount());
                }

                fromAccount.setBalance(fromAccount.getBalance().subtract(transferMoneyRequest.getAmount()));
                toAccount.setBalance(toAccount.getBalance().add(transferMoneyRequest.getAmount()));

                accountRepository.saveAll(List.of(fromAccount, toAccount));
            }
        }
    }

    private void validateRequest(TransferMoneyRequest transferMoneyRequest) {
        if (transferMoneyRequest.getFromAccount() == null || transferMoneyRequest.getToAccount() == null) {
            throw new IllegalArgumentException("From or To account can not be null.");
        }

        if (transferMoneyRequest.getFromAccount().compareTo(transferMoneyRequest.getToAccount()) == 0) {
            throw new IllegalArgumentException("From and To accounts are same. Please provide different accounts to transfer money.");
        }
    }

    @Override
    public void withdrawMoney(WithdrawMoneyRequest withdrawMoneyRequest) {
        synchronized (withdrawMoneyRequest.getAccountId().toString().intern()) {
            Account account = getAccountFromRepository(withdrawMoneyRequest.getAccountId());
            if (account.getBalance().compareTo(withdrawMoneyRequest.getAmount()) < 0) {
                throw new InsufficientBalanceException("Insufficient balance in account id: " + withdrawMoneyRequest.getAccountId());
            }
            account.setBalance(account.getBalance().subtract(withdrawMoneyRequest.getAmount()));
            accountRepository.save(account);
        }
    }

    @Override
    public void depositMoney(DepositMoneyRequest depositMoneyRequest) {
        synchronized (depositMoneyRequest.getAccountId().toString().intern()) {
            Account account = getAccountFromRepository(depositMoneyRequest.getAccountId());
            account.setBalance(account.getBalance().add(depositMoneyRequest.getAmount()));
            accountRepository.save(account);
        }
    }


    private Account getAccountFromRepository(UUID accountId) {
        synchronized (accountId.toString().intern()) {
            Account account = accountRepository.getAccountById(accountId);
            if (account == null) {
                throw new AccountNotFoundException("Account not exist for accountId: " + accountId);
            }
            return account;
        }
    }

    private Account createAccountFrom(CreateAccountRequest createAccountRequest) {
        return Account.builder()
                .id(UUID.randomUUID())
                .userId(createAccountRequest.getUserId())
                .balance(createAccountRequest.getBalance())
                .build();
    }

    private UUID findLargerAccountId(UUID fromAccount, UUID toAccount) {
        if (fromAccount.compareTo(toAccount) >= 0) {
            return fromAccount;
        }
        return toAccount;
    }

    private UUID findSmallerAccountId(UUID fromAccount, UUID toAccount) {
        if (fromAccount.compareTo(toAccount) > 0) {
            return toAccount;
        }
        return fromAccount;
    }
}
