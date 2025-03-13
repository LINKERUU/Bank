package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.User;
import com.bank.service.UserService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller for managing user-related operations.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  /**
   * Constructs a new UserController with the specified UserService.
   *
   * @param userService the service for managing users
   */
  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Retrieves all users.
   *
   * @return a list of all users
   */
  @GetMapping
  public List<User> findAllUsers() {
    return userService.findAllUsers();
  }

  /**
   * Retrieves a user by ID.
   *
   * @param id the ID of the user to retrieve
   * @return the user with the specified ID
   * @throws ResourceNotFoundException if the user is not found
   */
  @GetMapping("/{id}")
  public User findUserById(@PathVariable Long id) {
    return userService.findUserById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID "
                    + id + " не найден"));
  }

  /**
   * Creates a new user.
   *
   * @param user the user to create
   * @return the created user
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public User createUser(@RequestBody User user) {
    return userService.createUser(user);
  }

  /**
   * Creates multiple users in a batch.
   *
   * @param users the list of users to create
   * @return the list of created users
   */
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<User> createUsers(@RequestBody List<User> users) {
    return userService.createUsers(users);
  }

  /**
   * Updates an existing user.
   *
   * @param id the ID of the user to update
   * @param user the updated user details
   * @return the updated user
   * @throws ResourceNotFoundException if the user is not found
   */
  @PutMapping("/{id}")
  public User updateUser(@PathVariable Long id, @RequestBody User user) {
    if (userService.findUserById(id).isEmpty()) {
      throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
    }
    return userService.updateUser(id, user);
  }

  /**
   * Deletes a user by ID.
   *
   * @param id the ID of the user to delete
   * @throws ResourceNotFoundException if the user is not found
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteUser(@PathVariable Long id) {
    if (userService.findUserById(id).isEmpty()) {
      throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
    }
    userService.deleteUser(id);
  }
}