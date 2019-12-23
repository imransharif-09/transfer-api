package com.revolut.transfer.controller;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.revolut.transfer.request.CreateAccountRequest;
import com.revolut.transfer.request.DepositMoneyRequest;
import com.revolut.transfer.request.TransferMoneyRequest;
import com.revolut.transfer.request.WithdrawMoneyRequest;
import com.revolut.transfer.service.AccountService;
import org.eclipse.jetty.http.HttpStatus;
import spark.Request;
import spark.Response;

import javax.validation.Valid;
import java.util.UUID;

import static spark.Spark.*;

public class AccountController {

    private static final String BASE_URL = "/accounts";
    private AccountService accountService;

    @Inject
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    public void registerApiRoutes() {

        get(BASE_URL + "/:accountId", (request, response) -> {
            UUID accountId = UUID.fromString(request.params(":accountId"));
            return new Gson().toJson(accountService.getAccount(accountId));
        });

        post(BASE_URL, (request, response) -> {
            CreateAccountRequest createAccountRequest = new Gson().fromJson(request.body(), CreateAccountRequest.class);
            response.status(HttpStatus.CREATED_201);
            return new Gson().toJson(accountService.createAccount(createAccountRequest));
        });

        delete(BASE_URL, (request, response) -> {
            accountService.deleteAllAccounts();
            return new Gson().toJson("All accounts have been deleted");
        });

        post(BASE_URL + "/transfer", (Request request, Response response) -> {
            TransferMoneyRequest transferMoneyRequest = new Gson().fromJson(request.body(), TransferMoneyRequest.class);
            accountService.transferMoney(transferMoneyRequest);
            return new Gson().toJson("Money has been transferred successfully");
        });

        put(BASE_URL + "/withdraw", (request, response) -> {
            WithdrawMoneyRequest withdrawMoneyRequest = new Gson().fromJson(request.body(), WithdrawMoneyRequest.class);
            accountService.withdrawMoney(withdrawMoneyRequest);
            return new Gson().toJson("Amount has been withdrawal");
        });

        put(BASE_URL + "/deposit", (request, response) -> {
            DepositMoneyRequest depositMoneyRequest = new Gson().fromJson(request.body(), DepositMoneyRequest.class);
            accountService.depositMoney(depositMoneyRequest);
            return new Gson().toJson("Amount has been deposited");
        });
    }
}
