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


@Service
@Transactional
@Slf4j
@Generated
public class VisitCounterServiceImpl implements VisitCounterService {

  private final VisitRepository visitRepository;
  private final ConcurrentHashMap<String, AtomicLong> counterCache = new ConcurrentHashMap<>();

  @Autowired
  public VisitCounterServiceImpl(VisitRepository visitRepository) {
    this.visitRepository = visitRepository;
    initializeCacheFromDatabase();
  }

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

  private boolean shouldSkipCounting(String url) {
    return url.startsWith("/api/logs")
            || url.startsWith("/api/visits");
  }
}