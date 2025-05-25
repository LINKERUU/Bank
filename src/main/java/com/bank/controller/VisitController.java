package com.bank.controller;

import com.bank.service.VisitCounterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling visit count operations.
 * Provides endpoints for retrieving visit statistics.
 */
@RestController
@RequestMapping("/api/visits")
public class VisitController {

  private final VisitCounterService visitCounterService;

  /**
   * Constructs a new VisitController with the specified VisitCounterService.
   *
   * @param visitCounterService the service to handle visit counting operations
   */
  @Autowired
  public VisitController(VisitCounterService visitCounterService) {
    this.visitCounterService = visitCounterService;
  }

  /**
   * Retrieves the visit count for a specific URL.
   *
   * @param url the URL to get the visit count for
   * @return the number of visits for the specified URL
   */
  @GetMapping("/count")
  @Operation(summary = "Get visit count",
          description = "Returns the number of visits for the specified URL")
  @ApiResponse(responseCode = "200", description = "Data retrieved successfully")
  public long getVisitCount(@RequestParam String url) {
    return visitCounterService.getVisitCount(url);
  }

  /**
   * Retrieves all visit counts.
   *
   * @return a map containing URLs as keys and their visit counts as values
   */
  @GetMapping
  @Operation(summary = "Get all visit counts",
          description = "Returns visit statistics for all URLs")
  @ApiResponse(responseCode = "200", description = "Data retrieved successfully")
  public Map<String, Long> getAllVisits() {
    return visitCounterService.getAllVisits();
  }
}