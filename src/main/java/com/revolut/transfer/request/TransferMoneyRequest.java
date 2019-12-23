package com.revolut.transfer.request;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@JsonSerialize
public class TransferMoneyRequest {

    private UUID fromAccount;
    private UUID toAccount;
    private BigDecimal amount;
}
