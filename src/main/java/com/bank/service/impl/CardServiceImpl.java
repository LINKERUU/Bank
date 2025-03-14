package com.bank.service.impl;

import com.bank.model.Card;
import com.bank.repository.CardRepository;
import com.bank.service.CardService;
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

  /**
   * Constructor.
   */
  @Autowired
  public CardServiceImpl(CardRepository cardRepository) {
    this.cardRepository = cardRepository;
  }

  @Override
  public List<Card> findAllCards() {
    return cardRepository.findAll();
  }

  @Override
  public Optional<Card> findCardById(Long id) {
    return cardRepository.findById(id);
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
   * @throws RuntimeException if the card with the specified ID is not found
   */
  @Override
  @Transactional
  public Card updateCard(Long id, Card card) {
    Card existingCard = cardRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Card not found"));

    // Обновляем только те поля, которые переданы в DTO
    if (card.getCardNumber() != null) {
      existingCard.setCardNumber(card.getCardNumber());
    }
    if (card.getExpirationDate() != null) {
      existingCard.setExpirationDate(card.getExpirationDate());
    }
    if (card.getCvv() != null) {
      existingCard.setCvv(card.getCvv());
    }
    if (card.getBalance() != null) {
      existingCard.setBalance(card.getBalance());
    }

    // Поле user не обновляется, остается прежним
    return cardRepository.save(existingCard);
  }

  @Override
  @Transactional
  public void deleteCard(Long id) {
    cardRepository.deleteById(id);
  }
}