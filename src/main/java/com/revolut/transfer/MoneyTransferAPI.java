package com.revolut.transfer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.revolut.transfer.config.BindingConfig;
import com.revolut.transfer.controller.AccountController;
import com.revolut.transfer.execption.AccountAlreadyExistsException;
import com.revolut.transfer.execption.AccountNotFoundException;
import com.revolut.transfer.execption.InsufficientBalanceException;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.eclipse.jetty.http.HttpStatus;

import static spark.Spark.exception;

@SwaggerDefinition(host = "localhost:4567",//
        info = @Info(description = "Transfer money from one account to another account.",
                version = "1.0",
                title = "Money transfer API"),
        schemes = {SwaggerDefinition.Scheme.HTTP},
        consumes = {"application/json"},
        produces = {"application/json"},
        tags = {@Tag(name = "swagger")})
public class MoneyTransferAPI {

    public static void main(String[] args) {
        setExceptionHandlers();
        startApplication();
    }


    private static void startApplication() {
        Injector injector = Guice.createInjector(new BindingConfig());
        AccountController accountController = injector.getInstance(AccountController.class);
        accountController.registerApiRoutes();
    }

    private static void setExceptionHandlers() {
        exception(AccountAlreadyExistsException.class, (ex, request, response) -> {
            response.status(HttpStatus.CONFLICT_409);
            response.body(ex.getMessage());
        });

        exception(AccountNotFoundException.class, (ex, request, response) -> {
            response.status(HttpStatus.NOT_FOUND_404);
            response.body(ex.getMessage());
        });

        exception(InsufficientBalanceException.class, (ex, request, response) -> {
            response.status(HttpStatus.BAD_REQUEST_400);
            response.body(ex.getMessage());
        });

        exception(IllegalArgumentException.class, (ex, request, response) -> {
            response.status(HttpStatus.BAD_REQUEST_400);
            response.body(ex.getMessage());
        });


    }
}
