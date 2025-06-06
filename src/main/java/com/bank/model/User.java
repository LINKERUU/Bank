package com.bank.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Represents a user entity in the banking system.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull(message = "First name cannot be null")
  @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
  @Column(name = "first_name")
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @NotNull(message = "Email cannot be null")
  @Email(message = "Email should be valid")
  @Column(name = "email")
  private String email;

  @Column(name = "phone", unique = true, nullable = false, length = 20)
  private String phone;

  @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
  private String passwordHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @JsonBackReference
  @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
  private Set<Account> accounts;

}