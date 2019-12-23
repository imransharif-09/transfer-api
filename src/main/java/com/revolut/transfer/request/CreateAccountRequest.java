package com.revolut.transfer.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
@JsonSerialize
public class CreateAccountRequest {
    private String userId;
    private BigDecimal balance;
}
