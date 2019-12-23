package com.revolut.transfer.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@JsonSerialize
public class AccountResponse {

    private UUID accountId;
    private String userId;
    private BigDecimal balance;
}
