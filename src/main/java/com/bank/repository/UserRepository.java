package com.bank.repository;

import com.bank.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing {@link User} entities.
 * Provides methods to interact with the database, including
 * fetching users with their associated accounts.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  /**
   * Finds a user by ID and eagerly fetches their associated accounts.
   *
   * @param id the ID of the user to retrieve
   * @return an {@link Optional} containing the user if found, otherwise empty
   */
  @EntityGraph(attributePaths = {"accounts"})
  // Eagerly loads accounts with the user
  Optional<User> findById(Long id);

  boolean existsByLogin(String login);

  Optional<User> findByLogin(String login);

  Optional<User> findByEmail(String email);

  /**
   * Checks whether a user with the given email exists in the database.
   * Validates that the email is not null and is in a valid format.
   *
   * @param email the email address to check (must not be null and must be valid)
   * @return true if a user with the given email exists, false otherwise
   * @throws jakarta.validation.ConstraintViolationException if email is null or invalid
   */
  boolean existsByEmail(@NotNull(message = "Email cannot be null")
                        @Email(message = "Email should be valid") String email);

  /**
   * Checks whether a user with the given phone number exists in the database.
   *
   * @param phone the phone number to check
   * @return true if a user with the given phone number exists, false otherwise
   */
  boolean existsByPhone(String phone);

}