package com.revolut.transfer.data;

import com.revolut.transfer.model.Account;
import com.revolut.transfer.request.CreateAccountRequest;
import com.revolut.transfer.request.DepositMoneyRequest;
import com.revolut.transfer.request.TransferMoneyRequest;
import com.revolut.transfer.request.WithdrawMoneyRequest;
import com.revolut.transfer.response.AccountResponse;

import java.math.BigDecimal;
import java.util.UUID;

public class MoneyTransferData {
    public static CreateAccountRequest getAccountInstance(String userId, double balance) {
        return CreateAccountRequest.builder().userId(userId)
                .balance(new BigDecimal(balance))
                .build();
    }

    public static DepositMoneyRequest getDepositMoneyInstance(UUID accountId, double amount) {
        return DepositMoneyRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal(amount))
                .build();
    }

    public static WithdrawMoneyRequest getWithDrawMoneyInstance(UUID accountId, double amount) {
        return WithdrawMoneyRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal(amount))
                .build();
    }

    public static TransferMoneyRequest getTransferMoneyInstance(UUID fromAccount, UUID toAccount, double amount) {
        return TransferMoneyRequest.builder()
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(new BigDecimal(amount))
                .build();
    }

    public static Account getAccountInstance(UUID accountId, String userId, double amount) {
        return Account.builder()
                .id(accountId)
                .userId(userId)
                .balance(new BigDecimal(amount))
                .build();
    }

    public static AccountResponse getAccountResponseInstance(UUID accountId, String userId, double amount) {
        return AccountResponse.builder()
                .accountId(accountId)
                .userId(userId)
                .balance(new BigDecimal(amount))
                .build();
    }
}
