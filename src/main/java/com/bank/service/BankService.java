package com.bank.service;

import com.bank.model.Account;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Сервис для работы с банковскими счетами.
 */
@Service
public final class BankService {

    /**
     * Стандартный баланс для первого аккаунта.
     */
    private static final double DEFAULT_BALANCE_1 = 2506.0;

    /**
     * Стандартный баланс для второго аккаунта.
     */
    private static final double DEFAULT_BALANCE_2 = 306.0;

    /**
     * Список банковских аккаунтов.
     */
    private final List<Account> accounts = List.of(
            new Account(1, "Gerald", DEFAULT_BALANCE_1),
            new Account(2, "Anna", DEFAULT_BALANCE_2)
    );


    /**
     * Получить список всех аккаунтов.
     *
     * @return список аккаунтов
     */
    public List<Account> findAllAccounts() {
        return accounts;
    }

    /**
     * Найти аккаунт по имени владельца.
     *
     * @param ownerName имя владельца
     * @return аккаунт
     */
    public Account getAccountByOwnerName(final String ownerName) {
        return accounts.stream()
                .filter(account -> account
                        .getOwnerName()
                        .equalsIgnoreCase(ownerName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Найти аккаунт по ID.
     *
     * @param accountId идентификатор аккаунта
     * @return аккаунт
     */
    public Account getAccountById(final int accountId) {
        return accounts.stream()
                .filter(account -> account.getAccountId() == accountId)
                .findFirst()
                .orElse(null);
    }
}
