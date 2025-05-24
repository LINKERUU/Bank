package com.bank.service;

import com.bank.dto.CardDto;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing card operations.
 */
public interface CardService {

  /**
   * Retrieves all cards.
   *
   * @return a list of all card DTOs
   */
  List<CardDto> findAllCards();

  /**
   * Retrieves a card by its ID.
   *
   * @param id the ID of the card to retrieve
   * @return an {@link Optional} containing the card DTO if found, otherwise empty
   */
  Optional<CardDto> findCardById(Long id);

  /**
   * Creates a new card.
   *
   * @return the created card DTO
   */
  CardDto createCard(CardDto cardDto);

  /**
   * Creates multiple cards in a batch.
   *
   * @param cards the list of card DTOs to create
   * @return the list of created card DTOs
   */
  List<CardDto> createCards(List<CardDto> cards);

  /**
   * Updates an existing card.
   *
   * @param id the ID of the card to update
   * @return the updated card DTO
   * @throws com.bank.exception.ResourceNotFoundException if card with given ID not found
   */
  CardDto updateCard(Long id, CardDto cardDto);

  /**
   * Deletes a card by its ID.
   *
   * @param id the ID of the card to delete
   * @throws com.bank.exception.ResourceNotFoundException if card with given ID not found
   */
  void deleteCard(Long id);
}