package com.bank.service.impl;

import com.bank.model.Visit;
import com.bank.repository.VisitRepository;
import com.bank.service.VisitCounterService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Generated;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link VisitCounterService} that maintains visit counts in memory
 * and periodically persists them to the database.
 */
@Service
@Transactional
@Slf4j
@Generated
public class VisitCounterServiceImpl implements VisitCounterService {

  private final VisitRepository visitRepository;
  private final ConcurrentHashMap<String, AtomicLong> counterCache = new ConcurrentHashMap<>();

  /**
   * Initializes the in-memory cache with data from the database.
   */
  @Autowired
  public VisitCounterServiceImpl(VisitRepository visitRepository) {
    this.visitRepository = visitRepository;
    initializeCacheFromDatabase();
  }

  /**
   * Initializes the in-memory cache with data from the database.
   */
  private void initializeCacheFromDatabase() {
    visitRepository.findAll().forEach(visit ->
            counterCache.put(visit.getUrl(), new AtomicLong(visit.getCount()))
    );
  }

  @Override
  public synchronized void incrementVisitCount(String url) {
    if (shouldSkipCounting(url)) {
      log.debug("Skipping URL: {}", url);
      return;
    }

    AtomicLong counter = counterCache.computeIfAbsent(url, k -> {
      Visit visit = visitRepository.findByUrl(url);
      long initialValue = (visit != null) ? visit.getCount() : 0;
      return new AtomicLong(initialValue);
    });
    counter.incrementAndGet();
    log.debug("Incremented count for URL: {}, new count: {}", url, counter.get());
  }

  @Override
  public long getVisitCount(String url) {
    if (shouldSkipCounting(url)) {
      return 0;
    }

    AtomicLong counter = counterCache.get(url);
    if (counter != null) {
      return counter.get();
    }
    Visit visit = visitRepository.findByUrl(url);
    return (visit != null) ? visit.getCount() : 0;
  }

  @Override
  public Map<String, Long> getAllVisits() {
    Map<String, Long> result = new LinkedHashMap<>();

    visitRepository.findAll().stream()
            .filter(visit -> !shouldSkipCounting(visit.getUrl()))
            .forEach(visit -> result.put(visit.getUrl(), visit.getCount()));

    counterCache.forEach((url, counter) -> {
      if (!shouldSkipCounting(url)) {
        result.put(url, counter.get());
      }
    });

    return result;
  }

  /**
   * Scheduled task that periodically saves the in-memory counters to the database.
   * Runs every 6 seconds.
   */
  @Scheduled(fixedRate = 6000)
  @Transactional
  public void saveCountersToDatabase() {
    List<Visit> visitsToSave = new ArrayList<>();

    counterCache.forEach((url, counter) -> {
      if (shouldSkipCounting(url)) {
        return;
      }

      Visit visit = visitRepository.findByUrl(url);
      if (visit == null) {
        visit = new Visit();
        visit.setUrl(url);
      }
      visit.setCount(counter.get());
      visit.setLastVisitedAt(LocalDateTime.now());
      visitsToSave.add(visit);
    });

    visitRepository.saveAll(visitsToSave);
  }

  /**
   * Determines if a URL should be excluded from visit counting.
   *
   * @param url the URL to check
   * @return true if the URL should be skipped, false otherwise
   */
  private boolean shouldSkipCounting(String url) {
    return url.startsWith("/api/logs")
            || url.startsWith("/api/visits");
  }
}