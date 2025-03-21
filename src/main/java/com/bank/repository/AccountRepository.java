package com.bank.repository;

import com.bank.model.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link Account} entities.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  /**
   * Finds accounts associated with a specific user email.
   *
   * @param email the email of the user to filter accounts by
   * @return a list of accounts associated with the specified email
   */
  @Query("SELECT a FROM Account a JOIN a.users u WHERE u.email = :email")
  List<Account> findByUserEmail(@Param("email") String email);

  //  @Query(
  //          value = "SELECT DISTINCT a.* FROM accounts a " +
  //                  "JOIN user_accounts ua ON a.id = ua.account_id " +
  //                  "JOIN users u ON ua.user_id = u.id " +
  //                  "WHERE u.email = :email",
  //          nativeQuery = true
  //  )
}