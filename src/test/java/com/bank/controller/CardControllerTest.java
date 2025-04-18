package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Card;
import com.bank.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

  @Mock
  private CardService cardService;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private CardController cardController;

  private Card validCard;
  private Card invalidCard;

  @BeforeEach
  void setUp() {
    validCard = new Card();
    validCard.setId(1L);
    validCard.setCardNumber("1234567890123456");
    validCard.setExpirationDate(YearMonth.now().plusYears(2));
    validCard.setCvv("123");

    invalidCard = new Card();
    invalidCard.setId(2L);
    invalidCard.setCardNumber("invalid");
    invalidCard.setExpirationDate(YearMonth.now().minusMonths(1));
    invalidCard.setCvv("1");
  }

  @Test
  void findAllCards_ShouldReturnAllCards() {
    // Arrange
    List<Card> expectedCards = Arrays.asList(validCard, new Card());
    when(cardService.findAllCards()).thenReturn(expectedCards);

    // Act
    List<Card> actualCards = cardController.findAllCards();

    // Assert
    assertEquals(expectedCards.size(), actualCards.size());
    verify(cardService, times(1)).findAllCards();
  }

  @Test
  void getCardById_WhenCardExists_ShouldReturnCard() {
    // Arrange
    when(cardService.findCardById(1L)).thenReturn(Optional.of(validCard));

    // Act
    ResponseEntity<Card> response = cardController.getCardById(1L);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(validCard, response.getBody());
  }

  @Test
  void getCardById_WhenCardNotExists_ShouldReturnNotFound() {
    // Arrange
    when(cardService.findCardById(anyLong())).thenReturn(Optional.empty());

    // Act
    ResponseEntity<Card> response = cardController.getCardById(99L);

    // Assert
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  void createCard_WithValidCard_ShouldReturnCreated() {
    // Arrange
    when(bindingResult.hasErrors()).thenReturn(false);
    when(cardService.createCard(validCard)).thenReturn(validCard);

    // Act
    ResponseEntity<?> response = cardController.createCard(validCard, bindingResult);

    // Assert
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(validCard, response.getBody());
  }

  @Test
  void createCard_WithServiceValidationException_ShouldPropagateException() {
    // Arrange
    when(bindingResult.hasErrors()).thenReturn(false);
    when(cardService.createCard(validCard)).thenThrow(new ValidationException("Validation failed"));

    // Act & Assert
    assertThrows(ValidationException.class, () -> cardController.createCard(validCard, bindingResult));
  }

  @Test
  void createCards_ShouldReturnCreatedCards() {
    // Arrange
    List<Card> cardsToCreate = Arrays.asList(validCard, validCard);
    when(cardService.createCards(cardsToCreate)).thenReturn(cardsToCreate);

    // Act
    List<Card> createdCards = cardController.createCards(cardsToCreate);

    // Assert
    assertEquals(cardsToCreate.size(), createdCards.size());
    verify(cardService, times(1)).createCards(cardsToCreate);
  }

  @Test
  void updateCard_WithValidData_ShouldReturnUpdatedCard() {
    // Arrange
    when(cardService.updateCard(1L, validCard)).thenReturn(validCard);

    // Act
    ResponseEntity<Card> response = cardController.updateCard(1L, validCard);

    // Assert
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(validCard, response.getBody());
  }

  @Test
  void updateCard_WithNonExistingId_ShouldThrowResourceNotFoundException() {
    // Arrange
    when(cardService.updateCard(anyLong(), any())).thenThrow(new ResourceNotFoundException("Card not found"));

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> cardController.updateCard(99L, validCard));
  }

  @Test
  void updateCard_WithInvalidData_ShouldThrowIllegalArgumentException() {
    // Arrange
    when(cardService.updateCard(anyLong(), any())).thenThrow(new IllegalArgumentException("Invalid data"));

    // Act & Assert
    assertThrows(IllegalArgumentException.class, () -> cardController.updateCard(1L, invalidCard));
  }

  @Test
  void deleteCard_WithExistingId_ShouldCallService() {
    // Act
    cardController.deleteCard(1L);

    // Assert
    verify(cardService, times(1)).deleteCard(1L);
  }

  @Test
  void deleteCard_WithNonExistingId_ShouldThrowResourceNotFoundException() {
    // Arrange
    doThrow(new ResourceNotFoundException("Card not found")).when(cardService).deleteCard(anyLong());

    // Act & Assert
    assertThrows(ResourceNotFoundException.class, () -> cardController.deleteCard(99L));
  }
}