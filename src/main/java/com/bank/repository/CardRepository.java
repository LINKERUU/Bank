package com.bank.repository;

import com.bank.dto.CardDto;
import com.bank.model.Card;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Card} entities.
 * Provides CRUD operations and custom queries for cards.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

  /**
   * Finds all cards and returns them as DTOs.
   *
   * @return list of card DTOs
   */
  @Query("SELECT new com.bank.dto.CardDto(c.id, c.cardNumber, "
          + "c.expirationDate, c.cvv, c.account.id, c.account.accountNumber) "
          + "FROM Card c")
  List<CardDto> findAllCardsAsDto();

  /**
   * Finds a card by ID and returns it as DTO.
   *
   * @param id the card ID
   * @return optional containing the card DTO if found
   */
  @Query("SELECT new com.bank.dto.CardDto(c.id, c.cardNumber, "
          + "c.expirationDate, c.cvv, c.account.id, c.account.accountNumber) "
          + "FROM Card c WHERE c.id = :id")
  Optional<CardDto> findCardByIdAsDto(Long id);

  /**
   * Saves a card DTO and returns the saved card as DTO.
   *
   * @param cardDto the card DTO to save
   * @return the saved card DTO
   */
  default CardDto saveCardDto(CardDto cardDto) {
    Card card = new Card();
    card.setId(cardDto.getId());
    card.setCardNumber(cardDto.getCardNumber());
    card.setExpirationDate(cardDto.getExpirationDate());
    card.setCvv(cardDto.getCvv());

    Card savedCard = save(card);

    return new CardDto(
            savedCard.getId(),
            savedCard.getCardNumber(),
            savedCard.getExpirationDate(),
            savedCard.getCvv(),
            savedCard.getAccount().getId(),
            savedCard.getAccount().getAccountNumber()
    );
  }

  /**
   * Saves all card DTOs and returns them as DTOs.
   *
   * @param cardDtos list of card DTOs to save
   * @return list of saved card DTOs
   */
  default List<CardDto> saveAllCardsAsDto(List<CardDto> cardDtos) {
    List<Card> cards = cardDtos.stream().map(dto -> {
      Card card = new Card();
      card.setId(dto.getId());
      card.setCardNumber(dto.getCardNumber());
      card.setExpirationDate(dto.getExpirationDate());
      card.setCvv(dto.getCvv());
      return card;
    }).toList();

    List<Card> savedCards = saveAll(cards);

    return savedCards.stream().map(card ->
            new CardDto(
                    card.getId(),
                    card.getCardNumber(),
                    card.getExpirationDate(),
                    card.getCvv(),
                    card.getAccount().getId(),
                    card.getAccount().getAccountNumber()
            )
    ).toList();
  }
}