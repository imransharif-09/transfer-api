package com.revolut.transfer.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
public class Account {
    private UUID id;
    private String userId;
    private BigDecimal balance;
}
