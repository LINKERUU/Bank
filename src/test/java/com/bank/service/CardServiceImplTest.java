package com.bank.service;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Card;
import com.bank.repository.CardRepository;
import com.bank.service.impl.CardServiceImpl;
import com.bank.utils.InMemoryCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

  @Mock
  private CardRepository cardRepository;

  @Mock
  private InMemoryCache<Long, Card> cardCache;

  @InjectMocks
  private CardServiceImpl cardService;

  private Card validCard;

  @BeforeEach
  void setUp() {
    validCard = new Card();
    validCard.setId(1L);
    validCard.setCardNumber("1234567890123456");
    validCard.setExpirationDate(YearMonth.now().plusYears(1));
    validCard.setCvv("123");
    validCard.setAccount(new Account());
  }

  // ========== findAllCards Tests ==========
  @Test
  void findAllCards_ShouldReturnEmptyListWhenNoCards() {
    when(cardRepository.findAll()).thenReturn(Collections.emptyList());

    List<Card> result = cardService.findAllCards();

    assertTrue(result.isEmpty());
    verify(cardRepository).findAll();
  }

  @Test
  void findAllCards_ShouldReturnAllCards() {
    when(cardRepository.findAll()).thenReturn(List.of(validCard));

    List<Card> result = cardService.findAllCards();

    assertEquals(1, result.size());
    assertEquals(validCard, result.getFirst());
    verify(cardRepository).findAll();
  }

  // ========== findCardById Tests ==========
  @Test
  void findCardById_ShouldReturnEmptyWhenNotFound() {
    when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

    Optional<Card> result = cardService.findCardById(99L);

    assertFalse(result.isPresent());
    verify(cardRepository).findById(99L);
  }

  @Test
  void findCardById_ShouldReturnCardWhenExists() {
    when(cardRepository.findById(1L)).thenReturn(Optional.of(validCard));

    Optional<Card> result = cardService.findCardById(1L);

    assertTrue(result.isPresent());
    assertEquals(validCard, result.get());
    verify(cardRepository).findById(1L);
  }

  @Test
  void findCardById_ShouldUseCacheWhenAvailable() {
    when(cardCache.get(1L)).thenReturn(validCard);

    Optional<Card> result = cardService.findCardById(1L);

    assertTrue(result.isPresent());
    assertEquals(validCard, result.get());
    verify(cardCache).get(1L);
    verify(cardRepository, never()).findById(any());
  }

  @Test
  void findCardById_ShouldFallbackToRepositoryWhenNotInCache() {
    when(cardCache.get(1L)).thenReturn(null);
    when(cardRepository.findById(1L)).thenReturn(Optional.of(validCard));

    Optional<Card> result = cardService.findCardById(1L);

    assertTrue(result.isPresent());
    assertEquals(validCard, result.get());
    verify(cardCache).get(1L);
    verify(cardRepository).findById(1L);
  }

  // ========== createCard Tests ==========
  @Test
  void createCard_ShouldSaveValidCard() {
    when(cardRepository.save(validCard)).thenReturn(validCard);

    Card result = cardService.createCard(validCard);

    assertEquals(validCard, result);
    verify(cardRepository).save(validCard);
    verify(cardCache).put(validCard.getId(), validCard);
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"", " ", "12", "1234", "abc"})
  void createCard_ShouldThrowForInvalidCvv(String cvv) {
    validCard.setCvv(cvv);
    assertThrows(ValidationException.class, () -> cardService.createCard(validCard));
  }

  @Test
  void createCard_ShouldThrowForExpiredCard() {
    validCard.setExpirationDate(YearMonth.now().minusMonths(1));
    assertThrows(ValidationException.class, () -> cardService.createCard(validCard));
  }

  @Test
  void createCard_ShouldThrowForNullExpirationDate() {
    validCard.setExpirationDate(null);
    assertThrows(ValidationException.class, () -> cardService.createCard(validCard));
  }

  // ========== createCards Tests ==========
  @Test
  void createCards_ShouldReturnAllCardsWhenValid() {
    when(cardRepository.saveAll(anyList())).thenReturn(List.of(validCard));

    List<Card> result = cardService.createCards(List.of(validCard));

    assertEquals(1, result.size());
    assertEquals(validCard, result.getFirst());
    verify(cardRepository).saveAll(anyList());
    verify(cardCache).put(validCard.getId(), validCard);
  }

  @Test
  void createCards_ShouldThrowWhenAnyCardInvalid() {
    Card invalidCard = new Card();
    invalidCard.setCardNumber("1234");

    assertThrows(ValidationException.class,
            () -> cardService.createCards(List.of(validCard, invalidCard)));
  }

  // ========== updateCard Tests ==========
  @Test
  void updateCard_ShouldUpdateOnlyCardNumber() {
    Card existing = new Card();
    existing.setId(1L);
    existing.setCardNumber("1111222233334444");

    Card updates = new Card();
    updates.setCardNumber("5555666677778888");

    when(cardRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(cardRepository.save(existing)).thenReturn(existing);

    Card result = cardService.updateCard(1L, updates);

    assertEquals("5555666677778888", result.getCardNumber());
    assertEquals(existing.getExpirationDate(), result.getExpirationDate());
    assertEquals(existing.getCvv(), result.getCvv());
    verify(cardRepository).findById(1L);
    verify(cardRepository).save(existing);
    verify(cardCache).put(1L, existing);
  }

  @Test
  void updateCard_ShouldUpdateOnlyExpirationDate() {
    Card existing = new Card();
    existing.setId(1L);
    existing.setExpirationDate(YearMonth.now().plusYears(1));

    YearMonth newDate = YearMonth.now().plusYears(2);
    Card updates = new Card();
    updates.setExpirationDate(newDate);

    when(cardRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(cardRepository.save(existing)).thenReturn(existing);

    Card result = cardService.updateCard(1L, updates);

    assertEquals(newDate, result.getExpirationDate());
    assertEquals(existing.getCardNumber(), result.getCardNumber());
    assertEquals(existing.getCvv(), result.getCvv());
  }

  @Test
  void updateCard_ShouldUpdateOnlyCvv() {
    Card existing = new Card();
    existing.setId(1L);
    existing.setCvv("123");

    Card updates = new Card();
    updates.setCvv("999");

    when(cardRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(cardRepository.save(existing)).thenReturn(existing);

    Card result = cardService.updateCard(1L, updates);

    assertEquals("999", result.getCvv());
    assertEquals(existing.getCardNumber(), result.getCardNumber());
    assertEquals(existing.getExpirationDate(), result.getExpirationDate());
  }

  @Test
  void updateCard_ShouldNotUpdateWhenNoChanges() {
    when(cardRepository.findById(1L)).thenReturn(Optional.of(validCard));
    when(cardRepository.save(validCard)).thenReturn(validCard);

    Card result = cardService.updateCard(1L, new Card());

    assertEquals(validCard.getCardNumber(), result.getCardNumber());
    assertEquals(validCard.getExpirationDate(), result.getExpirationDate());
    assertEquals(validCard.getCvv(), result.getCvv());
    verify(cardRepository).save(validCard);
  }

  @Test
  void updateCard_ShouldThrowWhenCardNotFound() {
    when(cardRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> cardService.updateCard(99L, new Card()));
    verify(cardRepository).findById(99L);
    verify(cardRepository, never()).save(any());
  }

  @Test
  void updateCard_ShouldThrowWhenInvalidCardNumberProvided() {
    Card updates = new Card();
    updates.setCardNumber("invalid");

    when(cardRepository.findById(1L)).thenReturn(Optional.of(validCard));

    assertThrows(ValidationException.class,
            () -> cardService.updateCard(1L, updates));
  }

  @Test
  void updateCard_ShouldThrowWhenInvalidCvvProvided() {
    Card updates = new Card();
    updates.setCvv("12");

    when(cardRepository.findById(1L)).thenReturn(Optional.of(validCard));

    assertThrows(ValidationException.class,
            () -> cardService.updateCard(1L, updates));
  }

  @Test
  void updateCard_ShouldThrowWhenExpiredDateProvided() {
    Card updates = new Card();
    updates.setExpirationDate(YearMonth.now().minusMonths(1));

    when(cardRepository.findById(1L)).thenReturn(Optional.of(validCard));

    assertThrows(ValidationException.class,
            () -> cardService.updateCard(1L, updates));
  }


  @Test
  void deleteCard_ShouldThrowWhenCardNotFound() {
    when(cardRepository.existsById(99L)).thenReturn(false);

    Exception exception = assertThrows(ResourceNotFoundException.class,
            () -> cardService.deleteCard(99L));

    assertTrue(exception.getMessage().contains("99"));
    verify(cardRepository).existsById(99L);
    verify(cardRepository, never()).deleteById(any());
    verify(cardCache, never()).remove(any());
  }

  // ========== validateCard Tests ==========
  @Test
  void validateCard_ShouldNotThrowForValidCard() {
    assertDoesNotThrow(() -> cardService.validateCard(validCard));
  }

  @Test
  void validateCard_ShouldThrowWhenCardIsNull() {
    assertThrows(ValidationException.class, () -> cardService.validateCard(null));
  }

  // ========== validateCardNumber Tests ==========
  @ParameterizedTest
  @ValueSource(strings = {"4111111111111111", "5500000000000004", "6011111111111117"})
  void validateCardNumber_ShouldAcceptValidNumbers(String number) {
    assertDoesNotThrow(() -> cardService.validateCardNumber(number));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"", " ", "123", "123456789012345", "12345678901234567", "abcdefghijklmnop"})
  void validateCardNumber_ShouldRejectInvalidNumbers(String number) {
    assertThrows(ValidationException.class, () -> cardService.validateCardNumber(number));
  }

  // ========== validateExpirationDate Tests ==========
  @Test
  void validateExpirationDate_ShouldThrowForNullDate() {
    assertThrows(ValidationException.class,
            () -> cardService.validateExpirationDate(null));
  }

  @Test
  void validateExpirationDate_ShouldThrowForPastDate() {
    assertThrows(ValidationException.class,
            () -> cardService.validateExpirationDate(YearMonth.now().minusMonths(1)));
  }

  @Test
  void validateExpirationDate_ShouldAcceptCurrentMonth() {
    assertDoesNotThrow(() -> cardService.validateExpirationDate(YearMonth.now()));
  }

  @Test
  void validateExpirationDate_ShouldAcceptFutureDate() {
    assertDoesNotThrow(() -> cardService.validateExpirationDate(YearMonth.now().plusYears(1)));
  }

  // ========== validateCvv Tests ==========
  @Test
  void validateCvv_ShouldThrowForNullCvv() {
    assertThrows(ValidationException.class,
            () -> cardService.validateCvv(null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "12", "12345", "abc"})
  void validateCvv_ShouldRejectInvalidCvv(String cvv) {
    assertThrows(ValidationException.class, () -> cardService.validateCvv(cvv));
  }
}