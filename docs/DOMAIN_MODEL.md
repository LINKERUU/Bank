# Bank App - Domain Model Class Diagram
## Overview
This document describes the key entities and their relationships within the **Bank App** application domain, based on the actual database schema and system design. This model serves as the conceptual and technical foundation for the object-oriented implementation using Spring Boot + JPA + PostgreSQL.

## Class Diagram Schema
![Domain Model Class Diagram](shema/uml_diagram_class.png)

## Class Diagram Description
The core domain of **Bank App** revolves around users, their financial accounts, bank cards, transactions and auxiliary entities. The main entities are:

* **`Users`** – Представляет зарегистрированного пользователя системы (аутентификация и авторизация).
* **`Accounts`** – Банковский счёт пользователя (основной финансовый объект).
* **`Cards`** – Банковские карты, привязанные к конкретному счёту.
* **`Transactions`** – Все финансовые операции (поступления и списания).
* **`user_accounts`** – Связующая таблица (Many-to-Many), позволяющая одному пользователю иметь несколько счетов.

### Key Relationships Explained:

1. **Users → user_accounts (One-to-Many)**  
   Один пользователь может иметь много счетов.

2. **Accounts → user_accounts (One-to-Many)**  
   Один счёт может принадлежать только одному пользователю (через связующую таблицу).

3. **Accounts → Cards (One-to-Many)**  
   К одному банковскому счёту может быть привязано несколько карт.  
   Каждая карта принадлежит ровно одному счёту (`account_id` → `Accounts.id`).

4. **Accounts → Transactions (One-to-Many)**  
   По одному счёту может проходить множество транзакций.  
   Каждая транзакция привязана к конкретному счёту (`account_id` → `Accounts.id`).

5. **Users → Accounts (Many-to-Many через user_accounts)**  
   Реализована через промежуточную таблицу `user_accounts`, содержащую внешние ключи `user_id` и `account_id`.

### Attributes:

**`Users`**  
- `id` (PK, bigint)  
- `created_at` (timestamp)  
- `phone` (varchar)  
- `last_name` (varchar)  
- `login` (varchar, уникальный)  
- `email` (varchar, уникальный)  
- `first_name` (varchar)  
- `password_hash` (text, BCrypt)

**`Accounts`**  
- `id` (PK, bigint)  
- `balance` (double precision)  
- `created_at` (timestamp)  
- `account_number` (varchar(20), уникальный)

**`Cards`**  
- `id` (PK, bigint)  
- `account_id` (FK → Accounts.id)  
- `card_number` (varchar(255), маскируется при выводе)  
- `cvv` (varchar(255), зашифровано)  
- `expiration_date` (bytea или varchar)

**`Transactions`**  
- `id` (PK, bigint)  
- `amount` (double precision)  
- `account_id` (FK → Accounts.id)  
- `transaction_date` (timestamp)  
- `description` (varchar(255))  
- `transaction_type` (varchar – INCOME / EXPENSE)

**`user_accounts`** (junction table)  
- `user_id` (FK → Users.id)  
- `account_id` (FK → Accounts.id)  
- Composite primary key (user_id, account_id)

This domain model fully supports all required use cases: registration/authentication, multi-account management, card and transaction operations, as well as analytical statistics based on transaction history.