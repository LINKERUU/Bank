package com.bank.service.impl;

import com.bank.dto.CardDto;
import com.bank.exception.ResourceNotFoundException;
import com.bank.exception.ValidationException;
import com.bank.model.Account;
import com.bank.model.Card;
import com.bank.repository.AccountRepository;
import com.bank.repository.CardRepository;
import com.bank.service.CardService;
import com.bank.utils.InMemoryCache;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link CardService} providing card-related operations.
 */
@Service
public class CardServiceImpl implements CardService {

  private final CardRepository cardRepository;
  private final AccountRepository accountRepository;
  private final InMemoryCache<Long, CardDto> cardCache;

  /**
   * Constructs a new CardServiceImpl with required dependencies.
   *
   * @param cardRepository the card repository
   * @param accountRepository the account repository
   * @param cardCache the card cache
   */
  @Autowired
  public CardServiceImpl(CardRepository cardRepository,
                         AccountRepository accountRepository,
                         InMemoryCache<Long, CardDto> cardCache) {
    this.cardRepository = cardRepository;
    this.accountRepository = accountRepository;
    this.cardCache = cardCache;
  }

  /**
   * Retrieves all cards as DTOs.
   *
   * @return list of card DTOs
   */
  @Override
  @Transactional(readOnly = true)
  public List<CardDto> findAllCards() {
    return cardRepository.findAll().stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
  }

  /**
   * Finds a card by ID.
   *
   * @param id the card ID
   * @return optional containing the card DTO if found
   */
  @Override
  @Transactional(readOnly = true)
  public Optional<CardDto> findCardById(Long id) {
    CardDto cachedCard = cardCache.get(id);
    if (cachedCard != null) {
      return Optional.of(cachedCard);
    }

    Optional<CardDto> card = cardRepository.findById(id)
            .map(this::convertToDto);
    card.ifPresent(c -> cardCache.put(id, c));
    return card;
  }

  /**
   * Creates a new card.
   *
   * @param cardDto the card DTO to create
   * @return the created card DTO
   * @throws ValidationException if validation fails
   */
  @Override
  @Transactional
  public CardDto createCard(CardDto cardDto) {
    validateCard(cardDto);
    Card card = convertToEntity(cardDto);

    Account account = accountRepository.findById(cardDto.getAccountId())
            .orElseThrow(() -> new ValidationException(
                    "Account not found with id: " + cardDto.getAccountId()));
    card.setAccount(account);

    Card savedCard = cardRepository.save(card);
    CardDto savedCardDto = convertToDto(savedCard);
    cardCache.put(savedCardDto.getId(), savedCardDto);
    return savedCardDto;
  }

  /**
   * Creates multiple cards.
   *
   * @param cardsDto list of card DTOs to create
   * @return list of created card DTOs
   * @throws ValidationException if validation fails
   */
  @Override
  @Transactional
  public List<CardDto> createCards(List<CardDto> cardsDto) {
    cardsDto.forEach(this::validateCard);

    List<Card> cards = cardsDto.stream()
            .map(dto -> {
              Card card = convertToEntity(dto);
              Account account = accountRepository.findById(dto.getAccountId())
                      .orElseThrow(() -> new ValidationException(
                              "Account not found with id: " + dto.getAccountId()));
              card.setAccount(account);
              return card;
            })
            .collect(Collectors.toList());

    List<Card> savedCards = cardRepository.saveAll(cards);
    List<CardDto> savedCardsDto = savedCards.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());

    savedCardsDto.forEach(c -> cardCache.put(c.getId(), c));
    return savedCardsDto;
  }

  /**
   * Updates an existing card.
   *
   * @param id the card ID to update
   * @param cardDto the card DTO with updated data
   * @return the updated card DTO
   * @throws ResourceNotFoundException if card not found
   * @throws ValidationException if validation fails
   */
  @Override
  @Transactional
  public CardDto updateCard(Long id, CardDto cardDto) {
    Card existingCard = cardRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Card with ID " + id + " not found"));

    if (cardDto.getCardNumber() != null) {
      validateCardNumber(cardDto.getCardNumber());
      existingCard.setCardNumber(cardDto.getCardNumber());
    }
    if (cardDto.getExpirationDate() != null) {
      validateExpirationDate(cardDto.getExpirationDate());
      existingCard.setExpirationDate(cardDto.getExpirationDate());
    }
    if (cardDto.getCvv() != null) {
      validateCvv(cardDto.getCvv());
      existingCard.setCvv(cardDto.getCvv());
    }
    if (cardDto.getAccountId() != null
            && (existingCard.getAccount() == null
            || !existingCard.getAccount().getId().equals(cardDto.getAccountId()))) {
      Account account = accountRepository.findById(cardDto.getAccountId())
              .orElseThrow(() -> new ValidationException(
                      "Account not found with id: " + cardDto.getAccountId()));
      existingCard.setAccount(account);
    }

    Card updatedCard = cardRepository.save(existingCard);
    CardDto updatedCardDto = convertToDto(updatedCard);
    cardCache.put(id, updatedCardDto);
    return updatedCardDto;
  }

  private CardDto convertToDto(Card card) {
    CardDto cardDto = new CardDto();
    cardDto.setId(card.getId());
    cardDto.setCardNumber(card.getCardNumber());
    cardDto.setExpirationDate(card.getExpirationDate());
    cardDto.setCvv(card.getCvv());
    cardDto.setAccountId(card.getAccount().getId());
    cardDto.setAccountNumber(card.getAccount().getAccountNumber());
    return cardDto;
  }

  private Card convertToEntity(CardDto cardDto) {
    Card card = new Card();
    card.setId(cardDto.getId());
    card.setCardNumber(cardDto.getCardNumber());
    card.setExpirationDate(cardDto.getExpirationDate());
    card.setCvv(cardDto.getCvv());
    return card;
  }

  private void validateCard(CardDto cardDto) {
    if (cardDto == null) {
      throw new ValidationException("Card cannot be null");
    }

    validateCardNumber(cardDto.getCardNumber());
    validateExpirationDate(cardDto.getExpirationDate());
    validateCvv(cardDto.getCvv());

    if (cardDto.getAccountId() == null) {
      throw new ValidationException("The card must be linked to the account");
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

  /**
   * Deletes a card by ID.
   *
   * @param id the card ID to delete
   * @throws ResourceNotFoundException if card not found
   */
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