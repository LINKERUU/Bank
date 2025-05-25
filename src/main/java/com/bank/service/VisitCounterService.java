package com.bank.service;

import java.util.Map;

/**
 * Service interface for counting and tracking website visits.
 */
public interface VisitCounterService {

  /**
   * Increments the visit count for a specific URL.
   *
   * @param url the URL whose visit count should be incremented
   */
  void incrementVisitCount(String url);

  /**
   * Retrieves the visit count for a specific URL.
   *
   * @param url the URL to get the visit count for
   * @return the number of visits for the specified URL
   */
  long getVisitCount(String url);

  /**
   * Retrieves all visit counts for all URLs.
   *
   * @return a map containing URLs as keys and their visit counts as values
   */
  Map<String, Long> getAllVisits();
}