package com.bank.controller;

import com.bank.model.Card;
import com.bank.service.CardService;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * Controller for managing bank cards.
 */
@RestController
@RequestMapping("/api/cards")
public class CardController {

  private final CardService cardService;

  /**
   * Constructs a new CardController with the specified CardService.
   *
   * @param cardService the card service to be used by this controller
   */
  public CardController(CardService cardService) {
    this.cardService = cardService;
  }

  /**
   * Retrieves all cards.
   *
   * @return a list of all cards
   */
  @GetMapping
  public List<Card> findAllCards() {
    return cardService.findAllCards();
  }

  /**
   * Retrieves a card by its ID.
   *
   * @param id the ID of the card to retrieve
   * @return an {@link Optional} containing the card if found, otherwise empty
   */
  @GetMapping("/{id}")
  public Optional<Card> findCardById(@PathVariable Long id) {
    return cardService.findCardById(id);
  }

  /**
   * Creates a new card.
   *
   * @param card the card to create
   * @return the created card
   */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Card createCard(@RequestBody Card card) {
    return cardService.createCard(card);
  }

  /**
   * Creates multiple cards in a batch.
   *
   * @param cards the list of cards to create
   * @return the list of created cards
   */
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<Card> createCards(@RequestBody List<Card> cards) {
    return cardService.createCards(cards);
  }

  /**
   * Updates an existing card.
   *
   * @param id   the ID of the card to update
   * @param card the updated card details
   * @return the updated card
   */
  @PutMapping("/{id}")
  public Card updateCard(@PathVariable Long id, @RequestBody Card card) {
    return cardService.updateCard(id, card);
  }

  /**
   * Deletes a card by its ID.
   *
   * @param id the ID of the card to delete
   */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCard(@PathVariable Long id) {
    cardService.deleteCard(id);
  }
}