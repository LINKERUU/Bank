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
 * Implementation of the {@link CardService} interface for managing {@link Card} entities.
 */
@Service
public class CardServiceImpl implements CardService {

  private final CardRepository cardRepository;
  private final InMemoryCache<String, List<Card>> cardCache;
  private final InMemoryCache<Long, Card> cardByIdCache;

  /**
   * Constructs a new CardServiceImpl with the specified repository and caches.
   *
   * @param cardRepository the repository for managing cards
   * @param cardCache the cache for storing lists of cards
   * @param cardByIdCache the cache for storing cards by their ID
   */
  @Autowired
  public CardServiceImpl(CardRepository cardRepository,
                         InMemoryCache<String, List<Card>> cardCache,
                         InMemoryCache<Long, Card> cardByIdCache) {
    this.cardRepository = cardRepository;
    this.cardCache = cardCache;
    this.cardByIdCache = cardByIdCache;
  }

  @Override
  public List<Card> findAllCards() {
    String cacheKey = "all_cards";
    List<Card> cachedCards = cardCache.get(cacheKey);
    if (cachedCards != null) {
      return cachedCards;
    }

    List<Card> cards = cardRepository.findAll();
    cardCache.put(cacheKey, cards);
    return cards;
  }

  @Override
  public Optional<Card> findCardById(Long id) {
    Card cachedCard = cardByIdCache.get(id);
    if (cachedCard != null) {
      return Optional.of(cachedCard);
    }

    Optional<Card> card = cardRepository.findById(id);
    card.ifPresent(c -> cardByIdCache.put(id, c));
    return card;
  }

  @Override
  @Transactional
  public Card createCard(Card card) {
    return cardRepository.save(card);
  }

  @Override
  @Transactional
  public List<Card> createCards(List<Card> cards) {
    return cardRepository.saveAll(cards);
  }

  /**
   * Updates an existing card.
   *
   * @param id   the ID of the card to update
   * @param card the updated card details
   * @return the updated card
   * @throws ResourceNotFoundException if the card with the specified ID is not found
   */
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


    return cardRepository.save(existingCard);
  }

  @Override
  @Transactional
  public void deleteCard(Long id) {
    if (!cardRepository.existsById(id)) {
      throw new ResourceNotFoundException("Card not found with ID: " + id);
    }
    cardRepository.deleteById(id);

    // Очищаем кэш
    cardByIdCache.evict(id);
    cardCache.clear();
  }
}