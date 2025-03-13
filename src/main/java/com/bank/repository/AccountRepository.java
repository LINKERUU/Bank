package com.bank.repository;

import com.bank.model.Account;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository interface for managing {@link Account} entities.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  /**
   * Finds an account by its account number.
   *
   * @param accountNumber the account number to search for
   * @return an {@link Optional} containing the account if found, otherwise empty
   */
  Optional<Account> findByAccountNumber(String accountNumber);
}