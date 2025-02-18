/**
 * Пакет содержит REST-контроллеры для обработки HTTP-запросов,
 * связанных с управлением банковскими счетами.
 */
package com.bank.controller;


import com.bank.model.Account;
import com.bank.service.BankService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Контроллер для работы с банковскими счетами.
 */
@RestController
@RequestMapping("api/accounts")
public final class BankController {

    /**
     * Сервис для работы с аккаунтами.
     */
    private final BankService bankService;

    /**
     * Конструктор BankController.
     *
     * @param service сервис для работы с аккаунтами
     */
    public BankController(final BankService service) {
        this.bankService = service;
    }

    /**
     * Получить список всех аккаунтов.
     *
     * @return список аккаунтов
     */
    @GetMapping
    public List<Account> findAllAccounts() {
        return bankService.findAllAccounts();
    }

    /**
     * Получить аккаунт по имени владельца.
     *
     * @param ownerName имя владельца
     * @return аккаунт владельца
     */
    @GetMapping("/by-owner")
    public Account getAccountByOwnerName(@RequestParam final String ownerName) {
        return bankService.getAccountByOwnerName(ownerName);
    }

    /**
     * Получить аккаунт по ID.
     *
     * @param accountId идентификатор аккаунта
     * @return аккаунт
     */
    @GetMapping("/{accountId}")
    public Account getAccountById(@PathVariable final int accountId) {
        return bankService.getAccountById(accountId);
    }
}
