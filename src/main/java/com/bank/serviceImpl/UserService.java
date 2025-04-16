package com.bank.serviceImpl;

import com.bank.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link User} entities.
 * Provides methods for retrieving, creating, updating, and deleting users.
 */
public interface UserService {

  /**
   * Retrieves all users.
   *
   * @return a list of all users
   */
  List<User> findAllUsers();

  /**
   * Retrieves a user by ID.
   *
   * @param id the ID of the user to retrieve
   * @return an {@link Optional} containing the user if found, otherwise empty
   */
  Optional<User> findUserById(Long id);

  /**
   * Creates a new user.
   *
   * @param user the user to create
   * @return the created user
   */
  User createUser(User user);

  /**
   * Creates multiple users in a batch.
   *
   * @param users the list of users to create
   * @return the list of created users
   */
  List<User> createUsers(List<User> users);

  /**
   * Updates an existing user.
   *
   * @param id the ID of the user to update
   * @param user the updated user details
   * @return the updated user
   */
  User updateUser(Long id, User user);

  /**
   * Deletes a user by ID.
   *
   * @param id the ID of the user to delete
   */
  void deleteUser(Long id);
}