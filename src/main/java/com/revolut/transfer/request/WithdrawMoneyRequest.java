package com.revolut.transfer.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Getter
@JsonSerialize
public class WithdrawMoneyRequest {
    private UUID accountId;
    private BigDecimal amount;
}
