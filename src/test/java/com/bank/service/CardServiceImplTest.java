package com.bank.service;

import com.bank.exception.ValidationException;
import com.bank.model.Card;
import com.bank.repository.CardRepository;
import com.bank.service.impl.CardServiceImpl;
import com.bank.utils.InMemoryCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

  @Mock
  private CardRepository cardRepository;

  @Mock
  private InMemoryCache<Long, Card> cardCache;

  @InjectMocks
  private CardServiceImpl cardService;

  private Card createTestCard(Long id, String number, YearMonth expDate, String cvv) {
    Card card = new Card();
    card.setId(id);
    card.setCardNumber(number);
    card.setExpirationDate(expDate);
    card.setCvv(cvv);
    return card;
  }

  @Test
  void findCardById_ShouldReturnCachedCard() {
    Card card = createTestCard(1L, "1234567890123456", YearMonth.now().plusYears(1), "123");
    when(cardCache.get(1L)).thenReturn(card);

    Optional<Card> result = cardService.findCardById(1L);

    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
    verifyNoInteractions(cardRepository);
  }

  @Test
  void findCardById_ShouldFetchFromRepositoryWhenNotCached() {
    Card card = createTestCard(1L, "1234567890123456", YearMonth.now().plusYears(1), "123");
    when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

    Optional<Card> result = cardService.findCardById(1L);

    assertTrue(result.isPresent());
    verify(cardCache).put(1L, card);
  }

  @Test
  void createCard_ShouldValidateAndSaveCard() {
    Card card = createTestCard(null, "1234567890123456", YearMonth.now().plusYears(1), "123");
    Card savedCard = createTestCard(1L, "1234567890123456", YearMonth.now().plusYears(1), "123");

    when(cardRepository.save(card)).thenReturn(savedCard);

    Card result = cardService.createCard(card);

    assertNotNull(result.getId());
    verify(cardCache).put(1L, savedCard);
  }

  @Test
  void createCard_ShouldThrowValidationExceptionForInvalidCardNumber() {
    // Arrange
    Card invalidCard = createTestCard(null, "1234", YearMonth.now().plusYears(1), "123");

    // Act & Assert
    ValidationException exception = assertThrows(ValidationException.class,
            () -> cardService.createCard(invalidCard));

    // Проверяем стандартное сообщение валидации
    assertEquals("Card number must be 16 digits", exception.getMessage());
    verifyNoInteractions(cardRepository); // Убедимся, что сохранение не вызывалось
  }

  @Test
  void createCards_ShouldThrowWhenCardHasNoAccount() {
    Card cardWithoutAccount = createTestCard(null, "1111222233334444", YearMonth.now().plusYears(1), "111");

    assertThrows(ValidationException.class,
            () -> cardService.createCards(List.of(cardWithoutAccount)));

    verifyNoInteractions(cardRepository, cardCache);
  }

  @Test
  void updateCard_ShouldUpdateExistingCard() {
    Card existing = createTestCard(1L, "1111222233334444", YearMonth.now().plusYears(1), "111");
    Card updates = createTestCard(1L, null, YearMonth.now().plusYears(2), "999");

    when(cardRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(cardRepository.save(existing)).thenReturn(existing);

    Card result = cardService.updateCard(1L, updates);

    assertEquals("999", result.getCvv());
    assertEquals(YearMonth.now().plusYears(2), result.getExpirationDate());
    verify(cardCache).put(1L, existing);
  }

  @Test
  void deleteCard_ShouldDeleteAndEvictFromCache() {
    when(cardRepository.existsById(1L)).thenReturn(true);

    cardService.deleteCard(1L);

    verify(cardRepository).deleteById(1L);
    verify(cardCache).evict(1L);
  }

  @Test
  void validateCard_ShouldThrowForExpiredCard() {
    Card expiredCard = createTestCard(null, "1234567890123456", YearMonth.now().minusMonths(1), "123");

    assertThrows(ValidationException.class, () -> cardService.createCard(expiredCard));
  }
}