package com.bank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Account {

    /**
     * Уникальный идентификатор счета.
     */
    private int accountId;

    /**
     * Имя владельца счета.
     */
    private String ownerName;

    /**
     * Баланс счета.
     */
    private double balance;
}
