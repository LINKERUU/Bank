package com.bank.controller;

import com.bank.dto.CardDto;
import com.bank.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
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
 * Controller for handling card-related operations.
 * Provides endpoints for CRUD operations on bank cards.
 */
@RestController
@RequestMapping("/api/cards")
@Tag(name = "Card Controller", description = "API для работы с картами")
public class CardController {

  private final CardService cardService;

  /**
   * Retrieves all cards.
   *
   */
  public CardController(CardService cardService) {
    this.cardService = cardService;
  }

  /**
   * Retrieves all cards.
   *
   * @return list of all card DTOs
   */
  @Operation(summary = "Получить все карты",
          description = "Возвращает список всех банковских карт")
  @ApiResponse(responseCode = "200", description = "Карты успешно получены")
  @GetMapping
  public List<CardDto> findAllCards() {
    return cardService.findAllCards();
  }

  /**
   * Retrieves a card by its ID.
   *
   * @param id the ID of the card to retrieve
   * @return ResponseEntity containing the card DTO if found, or 404 if not found
   */
  @Operation(summary = "Получить карту по ID",
          description = "Возвращает карту по ее идентификатору")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Карта найдена"),
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
  })
  @GetMapping("/{id}")
  public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
    return cardService.findCardById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Creates a new card.
   *
   * @param cardDto the card data to create
   * @param bindingResult the binding result for validation
   * @return ResponseEntity containing the created card DTO or error messages
   */
  @Operation(summary = "Создать новую карту",
          description = "Создает новую банковскую карту")
  @ApiResponse(responseCode = "201", description = "Карта успешно создана")
  @ApiResponse(responseCode = "400", description = "Некорректные данные")
  @PostMapping
  public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto cardDto,
                                            BindingResult bindingResult) {

    if (bindingResult.hasErrors()) {
      List<String> errors = bindingResult.getFieldErrors()
              .stream()
              .map(FieldError::getDefaultMessage)
              .toList();
      return ResponseEntity.badRequest().body((CardDto) Map.of("errors", errors));
    }

    CardDto createdCard = cardService.createCard(cardDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
  }

  /**
   * Creates multiple cards in a batch.
   *
   * @param cards list of card DTOs to create
   * @return list of created card DTOs
   */
  @Operation(summary = "Массовое создание карт",
          description = "Создает несколько карт одновременно")
  @ApiResponse(responseCode = "201", description = "Карты успешно созданы")
  @PostMapping("/batch")
  @ResponseStatus(HttpStatus.CREATED)
  public List<CardDto> createCards(@RequestBody List<CardDto> cards) {
    return cardService.createCards(cards);
  }

  /**
   * Updates an existing card.
   *
   * @param id the ID of the card to update
   * @param cardDto the updated card data
   * @return ResponseEntity containing the updated card DTO
   */
  @Operation(summary = "Обновить карту", description = "Обновляет существующую карту")
  @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Карта успешно обновлена"),
    @ApiResponse(responseCode = "404", description = "Карта не найдена")
  })
  @PutMapping("/{id}")
  public ResponseEntity<CardDto> updateCard(@PathVariable Long id,
                                            @Valid @RequestBody CardDto cardDto) {
    try {
      CardDto updatedCard = cardService.updateCard(id, cardDto);
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