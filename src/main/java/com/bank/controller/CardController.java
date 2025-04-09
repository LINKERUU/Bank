package com.bank.controller;

import com.bank.model.Card;
import com.bank.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  @Operation(summary = "Получить все карты",
          description = "Возвращает список всех банковских карт")
  @ApiResponse(responseCode = "200", description = "Карты успешно получены")
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
  @Operation(summary = "Получить карту по ID",
          description = "Возвращает карту по ее идентификатору")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Карта найдена"),
      @ApiResponse(responseCode = "404", description = "Карта не найдена")
  })
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
  @Operation(summary = "Создать новую карту",
          description = "Создает новую банковскую карту")
  @ApiResponse(responseCode = "201", description = "Карта успешно создана")
  @ApiResponse(responseCode = "400", description = "Некорректные данные")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<Card> createCard(@Valid @RequestBody Card card) {
    Card createdCard = cardService.createCard(card);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
  }

  /**
   * Creates multiple cards in a batch.
   *
   * @param cards the list of cards to create
   * @return the list of created cards
   */
  @Operation(summary = "Массовое создание карт",
          description = "Создает несколько карт одновременно")
  @ApiResponse(responseCode = "201", description = "Карты успешно созданы")
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
  @Operation(summary = "Обновить карту", description = "Обновляет существующую карту")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Карта успешно обновлена"),
      @ApiResponse(responseCode = "404", description = "Карта не найдена")
  })
  @PutMapping("/{id}")
  public ResponseEntity<Card> updateCard(@PathVariable Long id, @Valid @RequestBody Card card) {
    try {
      Card updatedCard = cardService.updateCard(id, card);
      return ResponseEntity.ok(updatedCard);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid card data: " + e.getMessage());
    }
  }

  /**
   * Deletes a card by its ID.
   *
   * @param id the ID of the card to delete
   */
  @Operation(summary = "Удалить карту", description = "Удаляет карту по ее ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Карта успешно удалена"),
      @ApiResponse(responseCode = "404", description = "Карта не найдена")
  })
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCard(@PathVariable Long id) {
    cardService.deleteCard(id);
  }
}