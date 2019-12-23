package com.revolut.transfer.config;

import com.google.inject.AbstractModule;
import com.revolut.transfer.repository.AccountRepository;
import com.revolut.transfer.repository.AccountRepositoryImpl;
import com.revolut.transfer.service.AccountService;
import com.revolut.transfer.service.AccountServiceImpl;

public class BindingConfig extends AbstractModule {

    @Override
    protected void configure() {
        bind(AccountRepository.class).to(AccountRepositoryImpl.class);
        bind(AccountService.class).to(AccountServiceImpl.class);
    }
}
