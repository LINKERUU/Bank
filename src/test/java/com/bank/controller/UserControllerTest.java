package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.User;
import com.bank.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock
  private UserService userService;

  @InjectMocks
  private UserController userController;

  private User user1;
  private User user2;

  @BeforeEach
  void setUp() {
    user1 = new User();
    user1.setId(1L);
    user1.setFirstName("John");
    user1.setLastName("Doe");
    user1.setEmail("john.doe@example.com");

    user2 = new User();
    user2.setId(2L);
    user2.setFirstName("Jane");
    user2.setLastName("Smith");
    user2.setEmail("jane.smith@example.com");
  }

  @Test
  void findAllUsers_ShouldReturnAllUsers() {
    // Arrange
    List<User> users = Arrays.asList(user1, user2);
    when(userService.findAllUsers()).thenReturn(users);

    // Act
    List<User> result = userController.findAllUsers();

    // Assert
    assertEquals(2, result.size());
    verify(userService, times(1)).findAllUsers();
  }

  @Test
  void findUserById_WithValidId_ShouldReturnUser() {
    // Arrange
    when(userService.findUserById(1L)).thenReturn(Optional.of(user1));

    // Act
    User result = userController.findUserById(1L);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(userService, times(1)).findUserById(1L);
  }

  @Test
  void findUserById_WithInvalidId_ShouldThrowException() {
    // Arrange
    when(userService.findUserById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> userController.findUserById(99L));
    assertEquals("Not found User with id: 99", exception.getMessage());
  }

  @Test
  void createUser_WithValidData_ShouldReturnCreatedUser() {
    // Arrange
    when(userService.createUser(any(User.class))).thenReturn(user1);

    // Act
    ResponseEntity<User> response = userController.createUser(user1);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(1L, response.getBody().getId());
    verify(userService, times(1)).createUser(any(User.class));
  }

  @Test
  void createUser_WithInvalidData_ShouldThrowException() {
    // Arrange
    user1.setEmail(null);
    when(userService.createUser(any(User.class)))
            .thenThrow(new IllegalArgumentException("Email is required"));

    // Act & Assert
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userController.createUser(user1));
    assertTrue(exception.getMessage().contains("Invalid user data"));
  }

  @Test
  void createUsers_WithValidList_ShouldReturnCreatedUsers() {
    // Arrange
    List<User> users = Arrays.asList(user1, user2);
    when(userService.createUsers(anyList())).thenReturn(users);

    // Act
    List<User> result = userController.createUsers(users);

    // Assert
    assertEquals(2, result.size());
    verify(userService, times(1)).createUsers(anyList());
  }

  @Test
  void updateUser_WithValidData_ShouldReturnUpdatedUser() {
    // Arrange
    when(userService.findUserById(1L)).thenReturn(Optional.of(user1));
    when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user1);

    // Act
    User result = userController.updateUser(1L, user1);

    // Assert
    assertNotNull(result);
    assertEquals(1L, result.getId());
    verify(userService, times(1)).updateUser(eq(1L), any(User.class));
  }

  @Test
  void updateUser_WithNonExistentId_ShouldThrowException() {
    // Arrange
    when(userService.findUserById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> userController.updateUser(99L, user1));
    assertEquals("Not found User with id: 99", exception.getMessage());
  }

  @Test
  void deleteUser_WithValidId_ShouldNotThrowException() {
    // Arrange
    when(userService.findUserById(1L)).thenReturn(Optional.of(user1));
    doNothing().when(userService).deleteUser(1L);

    // Act & Assert
    assertDoesNotThrow(() -> userController.deleteUser(1L));
    verify(userService, times(1)).deleteUser(1L);
  }

  @Test
  void deleteUser_WithNonExistentId_ShouldThrowException() {
    // Arrange
    when(userService.findUserById(99L)).thenReturn(Optional.empty());

    // Act & Assert
    ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> userController.deleteUser(99L));
    assertEquals("Not found User with id: 99", exception.getMessage());
  }
}