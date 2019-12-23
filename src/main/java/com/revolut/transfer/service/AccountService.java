package com.revolut.transfer.service;

import com.revolut.transfer.request.CreateAccountRequest;
import com.revolut.transfer.request.DepositMoneyRequest;
import com.revolut.transfer.request.TransferMoneyRequest;
import com.revolut.transfer.request.WithdrawMoneyRequest;
import com.revolut.transfer.response.AccountResponse;

import java.util.UUID;

public interface AccountService {

    UUID createAccount(CreateAccountRequest createAccountRequest);

    AccountResponse getAccount(UUID accountID);

//    List<AccountResponse> getAllAccounts();

//    void deleteAccount(UUID accountID);

    void transferMoney(TransferMoneyRequest transferMoneyRequest);

    void withdrawMoney(WithdrawMoneyRequest withdrawMoneyRequest);

    void depositMoney(DepositMoneyRequest depositMoneyRequest);

    void deleteAllAccounts();
}
