package com.revolut.transfer.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.revolut.transfer.util.APiRequestHandler;
import com.revolut.transfer.util.ApiResponse;
import com.revolut.transfer.MoneyTransferAPI;
import com.revolut.transfer.response.AccountResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static com.revolut.transfer.data.MoneyTransferData.*;
import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;

public class AccountControllerTest {

    private Gson gson = new Gson();
    private APiRequestHandler APiRequestHandler = new APiRequestHandler();

    @Before
    public void setup() throws InterruptedException {
        MoneyTransferAPI.main(null);
        sleep(3000);
    }

    @Test
    public void testNewAccountCreation() {
        ApiResponse accountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, accountApiResponse.getStatus());
    }

    private ApiResponse mockAccountCreation(double balance) {
        String createAccountRequest = gson.toJson(getAccountInstance("Test User", balance));
        ApiResponse createAccountApiResponse = APiRequestHandler.send("POST", "accounts", createAccountRequest);
        return createAccountApiResponse;
    }

    private ApiResponse mockAccountSecondCreation(double balance) {
        String createAccountRequest = gson.toJson(getAccountInstance("Test User 2", balance));
        ApiResponse createAccountApiResponse = APiRequestHandler.send("POST", "accounts", createAccountRequest);
        return createAccountApiResponse;
    }

    @Test
    public void testDuplicateAccount() {
        ApiResponse firstAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, firstAccountApiResponse.getStatus());
        ApiResponse createAccountApiResponseDuplicate = mockAccountCreation(100);
        assertEquals(HttpStatus.CONFLICT_409, createAccountApiResponseDuplicate.getStatus());
    }

    @Test
    public void testGetAccount() {
        ApiResponse createAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse.getStatus());

        String accountId = createAccountApiResponse.jsonElement().getAsString();
        ApiResponse getAccountApiResponse = APiRequestHandler.send("GET", "accounts/" + accountId);
        assertEquals(HttpStatus.OK_200, getAccountApiResponse.getStatus());

        AccountResponse accountResponse = gson.fromJson(getAccountApiResponse.jsonElement(), AccountResponse.class);
        assertEquals(accountId, accountResponse.getAccountId().toString());
    }

    @Test
    public void testNonExistingAccount() {
        String accountId = UUID.randomUUID().toString();
        ApiResponse getAccountApiResponse = APiRequestHandler.send("GET", "accounts/" + accountId);
        assertEquals(HttpStatus.NOT_FOUND_404, getAccountApiResponse.getStatus());
    }

    @Test
    public void testDepositMoney() {

        ApiResponse createAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse.getStatus());

        String accountId = createAccountApiResponse.jsonElement().getAsString();
        String depositMoneyRequest = gson.toJson(getDepositMoneyInstance(UUID.fromString(accountId), 100));
        ApiResponse depositMoneyApiResponse = APiRequestHandler.send("PUT", "accounts/deposit", depositMoneyRequest);
        assertEquals(HttpStatus.OK_200, depositMoneyApiResponse.getStatus());

        ApiResponse getAccountApiResponse = APiRequestHandler.send("GET", "accounts/" + accountId);
        assertEquals(HttpStatus.OK_200, getAccountApiResponse.getStatus());

        AccountResponse accountResponse = gson.fromJson(getAccountApiResponse.jsonElement(), AccountResponse.class);
        assertEquals(accountId, accountResponse.getAccountId().toString());
        assertEquals(new BigDecimal(200), accountResponse.getBalance());
    }

    @Test
    public void testDepositMoneyInNonExistingAccount() {
        String accountId = UUID.randomUUID().toString();
        String depositMoneyRequest = gson.toJson(getDepositMoneyInstance(UUID.fromString(accountId), 100));
        ApiResponse depositMoneyApiResponse = APiRequestHandler.send("PUT", "accounts/deposit", depositMoneyRequest);
        assertEquals(HttpStatus.NOT_FOUND_404, depositMoneyApiResponse.getStatus());
    }

    @Test
    public void testWithdrawMoney() {
        ApiResponse createAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse.getStatus());

        String accountId = createAccountApiResponse.jsonElement().getAsString();
        String withDrawMoneyRequest = gson.toJson(getWithDrawMoneyInstance(UUID.fromString(accountId), 30));
        ApiResponse withDrawMoneyApiResponse = APiRequestHandler.send("PUT", "accounts/withdraw", withDrawMoneyRequest);
        assertEquals(HttpStatus.OK_200, withDrawMoneyApiResponse.getStatus());

        ApiResponse getAccountApiResponse = APiRequestHandler.send("GET", "accounts/" + accountId);
        assertEquals(HttpStatus.OK_200, getAccountApiResponse.getStatus());

        AccountResponse accountResponse = gson.fromJson(getAccountApiResponse.jsonElement(), AccountResponse.class);
        assertEquals(accountId, accountResponse.getAccountId().toString());
        assertEquals(new BigDecimal(70), accountResponse.getBalance());
    }

    @Test
    public void testWithdrawInsufficientMoney() {
        ApiResponse createAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse.getStatus());

        String accountId = createAccountApiResponse.jsonElement().getAsString();
        String withDrawMoneyRequest = gson.toJson(getWithDrawMoneyInstance(UUID.fromString(accountId), 600));
        ApiResponse withDrawMoneyApiResponse = APiRequestHandler.send("PUT", "accounts/withdraw", withDrawMoneyRequest);
        assertEquals(HttpStatus.BAD_REQUEST_400, withDrawMoneyApiResponse.getStatus());
    }

    @Test
    public void testWithdrawMoneyFromNonExistingAccount() {
        String accountId = UUID.randomUUID().toString();
        String withDrawMoneyRequest = gson.toJson(getWithDrawMoneyInstance(UUID.fromString(accountId), 100));
        ApiResponse withDrawMoneyApiResponse = APiRequestHandler.send("PUT", "accounts/withdraw", withDrawMoneyRequest);
        assertEquals(HttpStatus.NOT_FOUND_404, withDrawMoneyApiResponse.getStatus());
    }

    @Test
    public void testTransferMoney() {
        ApiResponse createAccountApiResponse_1 = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse_1.getStatus());

        ApiResponse createAccountApiResponse_2 = mockAccountSecondCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse_2.getStatus());

        String fromAccount = createAccountApiResponse_1.jsonElement().getAsString();
        String toAccount = createAccountApiResponse_2.jsonElement().getAsString();

        String transferMoneyRequest = gson.toJson(getTransferMoneyInstance(UUID.fromString(fromAccount), UUID.fromString(toAccount), 30));
        ApiResponse transferMoneyApiResponse = APiRequestHandler.send("POST", "accounts/transfer", transferMoneyRequest);
        assertEquals(HttpStatus.OK_200, transferMoneyApiResponse.getStatus());

        ApiResponse getFromAccountApiResponse = APiRequestHandler.send("GET", "accounts/" + fromAccount);
        assertEquals(HttpStatus.OK_200, getFromAccountApiResponse.getStatus());

        AccountResponse fromAccountResponse = gson.fromJson(getFromAccountApiResponse.jsonElement(), AccountResponse.class);
        assertEquals(fromAccount, fromAccountResponse.getAccountId().toString());
        assertEquals(new BigDecimal(70), fromAccountResponse.getBalance());

        ApiResponse getToAccountApiResponse = APiRequestHandler.send("GET", "accounts/" + toAccount);
        assertEquals(HttpStatus.OK_200, getToAccountApiResponse.getStatus());

        AccountResponse toAccountResponse = gson.fromJson(getToAccountApiResponse.jsonElement(), AccountResponse.class);
        assertEquals(toAccount, toAccountResponse.getAccountId().toString());
        assertEquals(new BigDecimal(130), toAccountResponse.getBalance());
    }

    @Test
    public void testTransferMoneyShouldThrow400WhenSameAccountsAreProvided() {
        ApiResponse createAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse.getStatus());

        String accountId = createAccountApiResponse.jsonElement().getAsString();

        String transferMoneyRequest = gson.toJson(getTransferMoneyInstance(UUID.fromString(accountId), UUID.fromString(accountId), 30));
        ApiResponse transferMoneyApiResponse = APiRequestHandler.send("POST", "accounts/transfer", transferMoneyRequest);
        assertEquals(HttpStatus.BAD_REQUEST_400, transferMoneyApiResponse.getStatus());
    }

    @Test
    public void testTransferMoneyShouldThrow400WhenFromAccountProvidedIsNull() {
        ApiResponse createAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse.getStatus());

        String accountId = createAccountApiResponse.jsonElement().getAsString();

        String transferMoneyRequest = gson.toJson(getTransferMoneyInstance(null, UUID.fromString(accountId), 30));
        ApiResponse transferMoneyApiResponse = APiRequestHandler.send("POST", "accounts/transfer", transferMoneyRequest);
        assertEquals(HttpStatus.BAD_REQUEST_400, transferMoneyApiResponse.getStatus());
    }

    @Test
    public void testTransferMoneyShouldThrow400WhenToAccountProvidedIsNull() {
        ApiResponse createAccountApiResponse = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse.getStatus());

        String accountId = createAccountApiResponse.jsonElement().getAsString();

        String transferMoneyRequest = gson.toJson(getTransferMoneyInstance(UUID.fromString(accountId), null, 30));
        ApiResponse transferMoneyApiResponse = APiRequestHandler.send("POST", "accounts/transfer", transferMoneyRequest);
        assertEquals(HttpStatus.BAD_REQUEST_400, transferMoneyApiResponse.getStatus());
    }

    @Test
    public void testTransferInsufficientMoney() {
        ApiResponse createAccountApiResponse_1 = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse_1.getStatus());

        ApiResponse createAccountApiResponse_2 = mockAccountSecondCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse_2.getStatus());


        String fromAccount = createAccountApiResponse_1.jsonElement().getAsString();
        String toAccount = createAccountApiResponse_2.jsonElement().getAsString();

        String transferMoneyRequest = gson.toJson(getTransferMoneyInstance(UUID.fromString(fromAccount), UUID.fromString(toAccount), 200));
        ApiResponse transferMoneyApiResponse = APiRequestHandler.send("POST", "accounts/transfer", transferMoneyRequest);
        assertEquals(HttpStatus.BAD_REQUEST_400, transferMoneyApiResponse.getStatus());
    }

    @Test
    public void testTransferInvalidAmount() {
        ApiResponse createAccountApiResponse_1 = mockAccountCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse_1.getStatus());

        ApiResponse createAccountApiResponse_2 = mockAccountSecondCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse_2.getStatus());


        String fromAccount = createAccountApiResponse_1.jsonElement().getAsString();
        String toAccount = createAccountApiResponse_2.jsonElement().getAsString();

        String transferMoneyRequest = gson.toJson(getTransferMoneyInstance(UUID.fromString(fromAccount), UUID.fromString(toAccount), -200));
        ApiResponse transferMoneyApiResponse = APiRequestHandler.send("POST", "accounts/transfer", transferMoneyRequest);
        assertEquals(HttpStatus.BAD_REQUEST_400, transferMoneyApiResponse.getStatus());
    }

    @Test
    public void testTransferMoneyInNonExistingAccount() {
        ApiResponse createAccountApiResponse_1 = mockAccountSecondCreation(100);
        assertEquals(HttpStatus.CREATED_201, createAccountApiResponse_1.getStatus());

        JsonElement json = createAccountApiResponse_1.jsonElement();

        String fromAccount = json.getAsString();
        String toAccount = UUID.randomUUID().toString();

        String transferMoneyRequest = gson.toJson(getTransferMoneyInstance(UUID.fromString(fromAccount), UUID.fromString(toAccount), 30));
        ApiResponse transferMoneyApiResponse = APiRequestHandler.send("POST", "accounts/transfer", transferMoneyRequest);
        assertEquals(HttpStatus.NOT_FOUND_404, transferMoneyApiResponse.getStatus());
    }

    @After
    public void after() {
        APiRequestHandler.send("DELETE", "accounts");
    }
}
