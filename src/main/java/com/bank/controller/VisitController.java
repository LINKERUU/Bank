package com.bank.controller;

import com.bank.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

  private final VisitCounterService visitCounterService;

  @Autowired
  public VisitController(VisitCounterService visitCounterService) {
    this.visitCounterService = visitCounterService;
  }

  // Получение счетчика по конкретному URL
  @GetMapping("/count")
  @Operation(summary = "Получить количество посещений",
          description = "Возвращает количество посещений для указанного URL")
  @ApiResponse(responseCode = "200", description = "Данные получены")
  public long getVisitCount(@RequestParam String url) {
    return visitCounterService.getVisitCount(url);
  }

  // Получение всех счетчиков
  @GetMapping
  @Operation(summary = "Получить все счетчики",
          description = "Возвращает статистику посещений по всем URL")
  @ApiResponse(responseCode = "200", description = "Данные получены")
  public Map<String, Long> getAllVisits() {
    return visitCounterService.getAllVisits();
  }
}