package com.bank.serviceImpl.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Card;
import com.bank.repository.CardRepository;
import com.bank.serviceImpl.CardService;
import com.bank.utils.InMemoryCache;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Implementation of {@link CardService} that provides business logic
 * for managing bank cards with caching support.
 * Handles CRUD operations for cards while maintaining cache consistency.
 */
@Service
public class CardServiceImpl implements CardService {

  private final CardRepository cardRepository;
  private final InMemoryCache<Long, Card> cardCache;

  /**
   * Constructs a CardServiceImpl with required dependencies.
   *
   * @param cardRepository the repository for card data access
   * @param cardCache the in-memory cache for card entities
   */
  @Autowired
  public CardServiceImpl(CardRepository cardRepository,
                         InMemoryCache<Long, Card> cardCache) {
    this.cardRepository = cardRepository;
    this.cardCache = cardCache;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Card> findAllCards() {
    // Не кэшируем список карт, так как он часто меняется
    return cardRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Card> findCardById(Long id) {
    Card cachedCard = cardCache.get(id);
    if (cachedCard != null) {
      return Optional.of(cachedCard);
    }

    Optional<Card> card = cardRepository.findById(id);
    card.ifPresent(c -> cardCache.put(id, c));
    return card;
  }

  @Override
  public Card createCard(Card card) {
    // Валидация номера карты
    if (card.getCardNumber() == null || card.getCardNumber().length() != 16) {
      throw new ValidationException("Card number must be 16 digits");
    }

    // Дополнительные проверки
    validateExpirationDate(card);
    validateCVV(card);

    Card savedCard = cardRepository.save(card);
    cardCache.put(savedCard.getId(), savedCard);
    return savedCard;
  }

  private void validateExpirationDate(Card card) {
    if (card.getExpirationDate() == null || card.getExpirationDate().isBefore(YearMonth.now())) {
      throw new ValidationException("Invalid expiration date");
    }
  }

  private void validateCVV(Card card) {
    if (card.getCvv() == null || card.getCvv().length() < 3 || card.getCvv().length() > 4) {
      throw new ValidationException("CVV must be 3 or 4 digits");
    }
  }

  @Override
  @Transactional
  public List<Card> createCards(List<Card> cards) {
    cards.forEach(this::validateCard);
    List<Card> savedCards = cardRepository.saveAll(cards);
    savedCards.forEach(c -> cardCache.put(c.getId(), c));
    return savedCards;
  }

  @Override
  @Transactional
  public Card updateCard(Long id, Card card) {
    Card existingCard = cardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Card with ID " + id + " not found"));

    if (card.getCardNumber() != null) {
      validateCardNumber(card.getCardNumber());
      existingCard.setCardNumber(card.getCardNumber());
    }
    if (card.getExpirationDate() != null) {
      validateExpirationDate(card.getExpirationDate());
      existingCard.setExpirationDate(card.getExpirationDate());
    }
    if (card.getCvv() != null) {
      validateCvv(card.getCvv());
      existingCard.setCvv(card.getCvv());
    }

    Card savedCard = cardRepository.save(existingCard);
    cardCache.put(id, savedCard);
    return savedCard;
  }

  private void validateCard(Card card) {
    if (card == null) {
      throw new ValidationException("Card cannot be null");
    }

    validateCardNumber(card.getCardNumber());
    validateExpirationDate(card.getExpirationDate());
    validateCvv(card.getCvv());

    if (card.getAccount() == null) {
      throw new ValidationException("The card must be linked to the account");
    }

    if (cardRepository.existsById(Long.valueOf(card.getCardNumber()))) {
      throw new ValidationException("A card with this number already exists");
    }
  }

  private void validateCardNumber(String cardNumber) {
    if (cardNumber == null) {
      throw new ValidationException("Card number cannot be null");
    }
    if (!cardNumber.matches("^\\d{16}$")) {
      throw new ValidationException("The card number must consist of 16 digits");
    }
  }

  private void validateExpirationDate(YearMonth expirationDate) {
    if (expirationDate == null) {
      throw new ValidationException("Expiration date cannot be null");
    }
    if (expirationDate.isBefore(YearMonth.now())) {
      throw new ValidationException("The card has expired");
    }
  }

  private void validateCvv(String cvv) {
    if (cvv == null) {
      throw new ValidationException("CVV cannot be null");
    }
    if (!cvv.matches("^\\d{3}$")) {
      throw new ValidationException("The CVV code must consist of 3 digits");
    }
  }

  @Override
  @Transactional
  public void deleteCard(Long id) {
    if (!cardRepository.existsById(id)) {
      throw new ResourceNotFoundException("Card not found with ID: " + id);
    }
    cardRepository.deleteById(id);
    cardCache.evict(id);
  }
}