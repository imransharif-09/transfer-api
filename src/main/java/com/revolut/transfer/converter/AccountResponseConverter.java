package com.revolut.transfer.converter;

import com.revolut.transfer.model.Account;
import com.revolut.transfer.response.AccountResponse;

public class AccountResponseConverter {

    public AccountResponse createFrom(Account account) {
        return AccountResponse.builder()
                .accountId(account.getId())
                .userId(account.getUserId())
                .balance(account.getBalance())
                .build();
    }
}
