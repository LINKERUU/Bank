package com.bank.repository;

import com.bank.model.User;
import java.util.Optional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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
  @EntityGraph(attributePaths = {"accounts"})  // Eagerly loads accounts with the user
  Optional<User> findById(Long id);

  boolean existsByEmail(@NotNull(message = "Email cannot be null") @Email(message = "Email should be valid") String email);

  boolean existsByPhone(String phone);
}