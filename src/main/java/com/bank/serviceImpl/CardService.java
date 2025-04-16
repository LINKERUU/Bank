package com.bank.serviceImpl;

import com.bank.model.Card;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing {@link Card} entities.
 */
public interface CardService {

  /**
   * Retrieves all cards.
   *
   * @return a list of all cards
   */
  List<Card> findAllCards();

  /**
   * Retrieves a card by its ID.
   *
   * @param id the ID of the card to retrieve
   * @return an {@link Optional} containing the card if found, otherwise empty
   */
  Optional<Card> findCardById(Long id);

  /**
   * Creates a new card.
   *
   * @param card the card to create
   * @return the created card
   */
  Card createCard(Card card);

  /**
   * Creates multiple cards in a batch.
   *
   * @param cards the list of cards to create
   * @return the list of created cards
   */
  List<Card> createCards(List<Card> cards);

  /**
   * Updates an existing card.
   *
   * @param id   the ID of the card to update
   * @param card the updated card details
   * @return the updated card
   */
  Card updateCard(Long id, Card card);

  /**
   * Deletes a card by its ID.
   *
   * @param id the ID of the card to delete
   */
  void deleteCard(Long id);
}