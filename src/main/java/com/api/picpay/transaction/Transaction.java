package com.api.picpay.transaction;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("TRANSACTIONS")
public record Transaction (
    @Id
    Long id,
    Long payer,
    Long payee,
    BigDecimal value,
    @CreatedDate
    LocalDate createdAt)
{
    public Transaction {
        value = value.setScale(2);
    }
}
