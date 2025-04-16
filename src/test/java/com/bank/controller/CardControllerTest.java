package com.bank.controller;

import com.bank.exception.ResourceNotFoundException;
import com.bank.model.Card;
import com.bank.serviceImpl.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CardControllerTest {

  private MockMvc mockMvc;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Mock
  private CardService cardService;

  @InjectMocks
  private CardController cardController;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(cardController).build();
  }

  private Card createTestCard(Long id, String number, YearMonth expDate, String cvv) {
    Card card = new Card();
    card.setId(id);
    card.setCardNumber(number);
    card.setExpirationDate(expDate);
    card.setCvv(cvv);
    return card;
  }

  // Unit tests
  @Test
  void findAllCards_ShouldReturnAllCards() throws Exception {
    Card card = createTestCard(1L, "1234567890123456", YearMonth.now().plusYears(1), "123");
    when(cardService.findAllCards()).thenReturn(List.of(card));

    mockMvc.perform(get("/api/cards"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].cardNumber").value("1234567890123456"));

    verify(cardService).findAllCards();
  }

  @Test
  void findCardById_ShouldReturnCard() throws Exception {
    Card card = createTestCard(1L, "1234567890123456", YearMonth.now().plusYears(1), "123");
    when(cardService.findCardById(1L)).thenReturn(Optional.of(card));

    mockMvc.perform(get("/api/cards/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.cvv").value("123"));

    verify(cardService).findCardById(1L);
  }

  @Test
  void findCardById_ShouldReturnNotFound() throws Exception {
    when(cardService.findCardById(1L)).thenReturn(Optional.empty());

    mockMvc.perform(get("/api/cards/1"))
            .andExpect(status().isNotFound());

    verify(cardService).findCardById(1L);
  }

  @Test
  void createCard_ShouldReturnCreatedResponse() throws Exception {
    Card card = createTestCard(null, "1234567890123456", YearMonth.now().plusYears(1), "123");
    Card savedCard = createTestCard(1L, "1234567890123456", YearMonth.now().plusYears(1), "123");

    when(cardService.createCard(any(Card.class))).thenReturn(savedCard);

    mockMvc.perform(post("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(card)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.cardNumber").value("1234567890123456"));

    verify(cardService).createCard(any(Card.class));
  }

  @Test
  void createCard_ShouldValidateInput() throws Exception {
    Card invalidCard = createTestCard(null, "1234", YearMonth.now().plusYears(1), "123");

    mockMvc.perform(post("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidCard)))
            .andExpect(status().isBadRequest());

    verifyNoInteractions(cardService);
  }

  @Test
  void createCards_ShouldReturnCreatedCards() throws Exception {
    Card card1 = createTestCard(null, "1111222233334444", YearMonth.now().plusYears(1), "111");
    Card card2 = createTestCard(null, "5555666677778888", YearMonth.now().plusYears(2), "222");
    List<Card> cards = List.of(card1, card2);

    when(cardService.createCards(anyList())).thenReturn(cards);

    mockMvc.perform(post("/api/cards/batch")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(cards)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$[0].cardNumber").value("1111222233334444"))
            .andExpect(jsonPath("$[1].cardNumber").value("5555666677778888"));

    verify(cardService).createCards(anyList());
  }

  @Test
  void updateCard_ShouldReturnUpdatedCard() throws Exception {
    Card updates = createTestCard(1L, null, YearMonth.now().plusYears(2), "999");
    Card updated = createTestCard(1L, "1234567890123456", YearMonth.now().plusYears(2), "999");

    when(cardService.updateCard(anyLong(), any(Card.class))).thenReturn(updated);

    mockMvc.perform(put("/api/cards/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cvv").value("999"))
            .andExpect(jsonPath("$.expirationDate").exists());

    verify(cardService).updateCard(anyLong(), any(Card.class));
  }

  @Test
  void updateCard_ShouldHandleNotFound() throws Exception {
    Card updates = createTestCard(1L, null, YearMonth.now().plusYears(2), "999");

    when(cardService.updateCard(anyLong(), any(Card.class)))
            .thenThrow(new ResourceNotFoundException("Card not found"));

    mockMvc.perform(put("/api/cards/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isNotFound());

    verify(cardService).updateCard(anyLong(), any(Card.class));
  }

  @Test
  void deleteCard_ShouldReturnNoContent() throws Exception {
    doNothing().when(cardService).deleteCard(1L);

    mockMvc.perform(delete("/api/cards/1"))
            .andExpect(status().isNoContent());

    verify(cardService).deleteCard(1L);
  }

  // Validation tests
  @Test
  void shouldRejectInvalidCardNumber() throws Exception {
    String invalidCardJson = """
        {
            "cardNumber": "1234",
            "expirationDate": "2030-12",
            "cvv": "123"
        }
        """;

    mockMvc.perform(post("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidCardJson))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectExpiredCard() throws Exception {
    String expiredCardJson = """
        {
            "cardNumber": "1234567890123456",
            "expirationDate": "2020-01",
            "cvv": "123"
        }
        """;

    mockMvc.perform(post("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(expiredCardJson))
            .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectInvalidCvv() throws Exception {
    String invalidCvvJson = """
        {
            "cardNumber": "1234567890123456",
            "expirationDate": "2030-12",
            "cvv": "12"
        }
        """;

    mockMvc.perform(post("/api/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidCvvJson))
            .andExpect(status().isBadRequest());
  }
}