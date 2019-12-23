package com.revolut.transfer.execption;

public class AccountAlreadyExistsException extends RuntimeException {

    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
