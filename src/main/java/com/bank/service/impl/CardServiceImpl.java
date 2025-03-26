package com.bank.service.impl;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Card;
import com.bank.repository.CardRepository;
import com.bank.service.CardService;
import com.bank.utils.InMemoryCache;
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
  @Transactional
  public Card createCard(Card card) {
    Card savedCard = cardRepository.save(card);
    cardCache.put(savedCard.getId(), savedCard);
    return savedCard;
  }

  @Override
  @Transactional
  public List<Card> createCards(List<Card> cards) {
    List<Card> savedCards = cardRepository.saveAll(cards);
    savedCards.forEach(c -> cardCache.put(c.getId(), c));
    return savedCards;
  }

  @Override
  @Transactional
  public Card updateCard(Long id, Card card) {
    Card existingCard = cardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Card not found with ID: " + id));

    if (card.getCardNumber() != null) {
      existingCard.setCardNumber(card.getCardNumber());
    }
    if (card.getExpirationDate() != null) {
      existingCard.setExpirationDate(card.getExpirationDate());
    }
    if (card.getCvv() != null) {
      existingCard.setCvv(card.getCvv());
    }

    Card savedCard = cardRepository.save(existingCard);
    cardCache.put(id, savedCard);
    return savedCard;
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