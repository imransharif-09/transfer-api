package com.revolut.transfer.service;

import com.revolut.transfer.data.MoneyTransferData;
import com.revolut.transfer.converter.AccountResponseConverter;
import com.revolut.transfer.execption.AccountAlreadyExistsException;
import com.revolut.transfer.execption.AccountNotFoundException;
import com.revolut.transfer.execption.InsufficientBalanceException;
import com.revolut.transfer.model.Account;
import com.revolut.transfer.repository.AccountRepository;
import com.revolut.transfer.response.AccountResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.UUID;

import static com.revolut.transfer.data.MoneyTransferData.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountServiceTest {

    private static final String TEST_USER_ID = "Test User";
    private static final String TEST_TO_USER = "Test To User";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountResponseConverter accountResponseConverter;

    private AccountServiceImpl underTest;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        underTest = new AccountServiceImpl(accountRepository, accountResponseConverter);
    }

    @Test
    public void testNewAccountCreationShouldReturnNewlyCreatedAccountIdWhenUserDoesnotExists() {
        when(accountRepository.isUserExists(anyString())).thenReturn(false);
        UUID result = underTest.createAccount(getAccountInstance(TEST_USER_ID, 20));
        verify(accountRepository, times(1)).save(any(Account.class));

        assertNotNull(result);
    }

    @Test(expected = AccountAlreadyExistsException.class)
    public void testDuplicateAccountCreationShouldThrowExceptionWhenUserAlreadyExistsISystem() {
        when(accountRepository.isUserExists(anyString())).thenReturn(true);
        underTest.createAccount(getAccountInstance(TEST_USER_ID, 120));
    }

    @Test
    public void testGetAccountShouldReturnAccountResponseWhenValidAccountIdProvided() {
        UUID accountId = UUID.randomUUID();
        String userId = TEST_USER_ID;
        Account account = getAccountInstance(accountId, userId, 20);

        AccountResponse response = MoneyTransferData.getAccountResponseInstance(accountId, userId, 20);

        when(accountRepository.getAccountById(accountId)).thenReturn(account);
        when(accountResponseConverter.createFrom(account)).thenReturn(response);

        AccountResponse result = underTest.getAccount(accountId);

        verify(accountRepository, times(1)).getAccountById(accountId);
        assertEquals(response, result);
    }

    @Test(expected = AccountNotFoundException.class)
    public void testGetAccountShouldThrowExceptionWhenNoAccountFound() {
        when(accountRepository.getAccountById(any(UUID.class))).thenReturn(null);
        underTest.getAccount(UUID.randomUUID());
    }

    @Test
    public void testDepositMoney() {
        UUID accountId = UUID.randomUUID();
        String userId = TEST_USER_ID;
        Account account = getAccountInstance(accountId, userId, 60);
        when(accountRepository.getAccountById(accountId)).thenReturn(account);

        underTest.depositMoney(getDepositMoneyInstance(accountId, 20));
        verify(accountRepository, times(1)).save(getAccountInstance(accountId, userId, 80));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testDepositMoneyShouldThrowExceptionWhenWrongAccountNumberIsProvided() {
        when(accountRepository.getAccountById(any(UUID.class))).thenReturn(null);
        underTest.depositMoney(getDepositMoneyInstance(UUID.randomUUID(), 10));
    }

    @Test
    public void testWithdrawMoney() {
        UUID accountId = UUID.randomUUID();
        String userId = TEST_USER_ID;
        Account account = getAccountInstance(accountId, userId, 60);
        when(accountRepository.getAccountById(accountId)).thenReturn(account);

        underTest.withdrawMoney(getWithDrawMoneyInstance(accountId, 20));
        verify(accountRepository, times(1)).save(getAccountInstance(accountId, userId, 40));
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testWithdrawMoneyShouldThrowExceptionWhenAccountHasInsufficientBalance() {
        UUID accountId = UUID.randomUUID();
        Account account = getAccountInstance(accountId, TEST_USER_ID, 160);
        when(accountRepository.getAccountById(accountId)).thenReturn(account);
        underTest.withdrawMoney(getWithDrawMoneyInstance(accountId, 190));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testWithdrawMoneyShouldThrowExceptionWhenProvidedAccountNotFound() {
        when(accountRepository.getAccountById(any(UUID.class))).thenReturn(null);
        underTest.withdrawMoney(getWithDrawMoneyInstance(UUID.randomUUID(), 10));
    }

    @Test
    public void testTransferMoney() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account fromAccount = getAccountInstance(fromAccountId, TEST_USER_ID, 60);
        fromAccount.setBalance(new BigDecimal(30));

        Account toAccount = getAccountInstance(toAccountId, TEST_USER_ID, 20);
        toAccount.setBalance(new BigDecimal(50));

        when(accountRepository.getAccountById(fromAccountId)).thenReturn(fromAccount);
        when(accountRepository.getAccountById(toAccountId)).thenReturn(toAccount);

        underTest.transferMoney(getTransferMoneyInstance(fromAccountId, toAccountId, 30));

        verify(accountRepository, times(1)).saveAll(Arrays.asList(fromAccount, toAccount));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferMoneyShouldThrowIllegalArgumentWhenSameFromAndToAccountsProvided() {
        UUID accountId = UUID.randomUUID();
        Account account = getAccountInstance(accountId, TEST_USER_ID, 60);
        account.setBalance(new BigDecimal(30));
        underTest.transferMoney(getTransferMoneyInstance(accountId, accountId, 30));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferMoneyShouldThrowIllegalArgumentWhenFromAccountIsNull() {
        underTest.transferMoney(getTransferMoneyInstance(null, UUID.randomUUID(), 30));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferMoneyShouldThrowIllegalArgumentWhenToAccountIsNull() {
        underTest.transferMoney(getTransferMoneyInstance(UUID.randomUUID(), null, 30));
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testTransferInsufficientMoney() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();

        Account fromAccount = getAccountInstance(fromAccountId, TEST_USER_ID, 60);
        Account toAccount = getAccountInstance(toAccountId, TEST_USER_ID, 20);

        when(accountRepository.getAccountById(fromAccountId)).thenReturn(fromAccount);
        when(accountRepository.getAccountById(toAccountId)).thenReturn(toAccount);

        underTest.transferMoney(getTransferMoneyInstance(fromAccountId, toAccountId, 80));
    }

    @Test(expected = AccountNotFoundException.class)
    public void testTransferMoneyInNonExistingAccount() {
        UUID fromAccountId = UUID.randomUUID();
        Account fromAccount = getAccountInstance(fromAccountId, TEST_USER_ID, 60);
        when(accountRepository.getAccountById(fromAccountId)).thenReturn(fromAccount);

        UUID toAccountId = UUID.randomUUID();
        when(accountRepository.getAccountById(toAccountId)).thenReturn(null);

        underTest.transferMoney(getTransferMoneyInstance(fromAccountId, toAccountId, 80));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTransferMoneyWhenNegativeAmountIsInRequest() {
        underTest.transferMoney(getTransferMoneyInstance(UUID.randomUUID(), UUID.randomUUID(), -80));
    }

    @Test(expected = InsufficientBalanceException.class)
    public void testTransferMoneyWhenNegativeAmountIsAvailableInFromAccount() {
        UUID fromAccountId = UUID.randomUUID();
        String fromUserId = TEST_USER_ID;
        Account fromAccount = getAccountInstance(fromAccountId, fromUserId, -60);
        when(accountRepository.getAccountById(fromAccountId)).thenReturn(fromAccount);

        UUID toAccountId = UUID.randomUUID();
        Account toAccount = getAccountInstance(toAccountId, TEST_TO_USER, 20);
        when(accountRepository.getAccountById(toAccountId)).thenReturn(toAccount);

        underTest.transferMoney(getTransferMoneyInstance(fromAccountId, toAccountId, 80));
    }
}
