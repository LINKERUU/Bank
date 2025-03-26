package com.bank.repository;

import com.bank.model.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for managing bank accounts.
 * Provides methods to access and manipulate account data.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  /**
   * Finds all accounts associated with a user by email.
   *
   * @param email the user's email address
   * @return list of accounts associated with the given email
   */
  @Query(value = "SELECT DISTINCT a.* FROM accounts a "
          + "JOIN user_accounts ua ON a.id = ua.account_id "
          + "JOIN users u ON ua.user_id = u.id "
          + "WHERE u.email = :email",
          nativeQuery = true)
  List<Account> findByUserEmail(@Param("email") String email);

  /**
   * Finds all accounts that have at least one card associated.
   *
   * @return list of accounts with cards
   */
  @Query("SELECT a FROM Account a WHERE a.cards IS NOT EMPTY")
  List<Account> findAccountsWithCards();
}