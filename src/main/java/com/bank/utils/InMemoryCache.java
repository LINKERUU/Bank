package com.bank.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A generic in-memory cache implementation with time-based and size-based eviction
 * that prioritizes keeping frequently accessed items.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
@SuppressWarnings("squid:S6829")
@Component
public class InMemoryCache<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryCache.class);
  private static final int DEFAULT_MAX_SIZE = 3;
  private static final int USAGE_THRESHOLD = 3; // Minimum accesses to be considered frequently used

  /**
   * Remove old cache.
   */
  public void remove(Object any) {
  }


  /**
   * Internal cache entry that holds the value, its expiration time and usage count.
   */
  private class CacheEntry {
    @Getter private final V value;
    @Getter private final long expiryTime;
    private int usageCount;

    CacheEntry(V value, long expiryTime) {
      this.value = value;
      this.expiryTime = expiryTime;
      this.usageCount = 0;
    }

    boolean isExpired() {
      return System.currentTimeMillis() >= expiryTime;
    }

    void incrementUsage() {
      this.usageCount++;
    }

    int getUsageCount() {
      return this.usageCount;
    }
  }

  private final Map<K, CacheEntry> cache;
  private final long ttlMillis;
  private final int maxSize;

  /**
   * Constructs a cache with default TTL of 5 minutes and default size limit.
   */
  public InMemoryCache() {
    this(300_000, DEFAULT_MAX_SIZE);
  }

  /**
   * Constructs a cache with specified time-to-live and default size limit.
   */
  public InMemoryCache(long ttlMillis) {
    this(ttlMillis, DEFAULT_MAX_SIZE);
  }

  /**
   * Constructs a cache with specified time-to-live and size limit.
   */
  public InMemoryCache(long ttlMillis, int maxSize) {
    if (maxSize <= 0) {
      throw new IllegalArgumentException("Cache size must be positive");
    }
    this.ttlMillis = ttlMillis;
    this.maxSize = maxSize;
    this.cache = new HashMap<>(maxSize);

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(this::evictExpiredEntries,
            ttlMillis, ttlMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Retrieves a value from the cache and updates its usage count.
   */
  public V get(K key) {
    synchronized (cache) {
      CacheEntry entry = cache.get(key);
      if (entry == null) {
        logger.debug("Cache miss for key: {}", key);
        return null;
      }
      if (entry.isExpired()) {
        logger.debug("Cache entry expired for key: {}", key);
        cache.remove(key);
        return null;
      }
      entry.incrementUsage();
      logger.debug("Cache hit for key: {} (used {} times)", key, entry.getUsageCount());
      return entry.getValue();
    }
  }

  /**
   * Associates the specified value with the specified key in this cache.
   * Evicts least valuable entries if cache is full.
   */
  public void put(K key, V value) {
    if (key == null || value == null) {
      throw new IllegalArgumentException("Key and value cannot be null");
    }

    synchronized (cache) {
      // Check if we need to evict before adding new entry
      if (cache.size() >= maxSize) {
        evictLeastValuableEntry();
      }

      CacheEntry entry = new CacheEntry(value, System.currentTimeMillis() + ttlMillis);
      cache.put(key, entry);
      logger.debug("Cache put for key: {}", key);
    }
  }

  /**
   * Evicts the least valuable entry based on usage count and recency.
   */
  private void evictLeastValuableEntry() {
    K keyToRemove = null;
    int lowestUsage = Integer.MAX_VALUE;
    long oldestAccess = Long.MAX_VALUE;

    for (Map.Entry<K, CacheEntry> entry : cache.entrySet()) {
      // Skip frequently used items (above threshold)
      if (entry.getValue().getUsageCount() >= USAGE_THRESHOLD) {
        continue;
      }

      // Among remaining, find last used
      if (entry.getValue().getUsageCount() < lowestUsage
              || (entry.getValue().getUsageCount() == lowestUsage
              && entry.getValue().expiryTime < oldestAccess)) {
        keyToRemove = entry.getKey();
        lowestUsage = entry.getValue().getUsageCount();
        oldestAccess = entry.getValue().expiryTime;
      }
    }

    // If all items are frequently used, fall back to LRU
    if (keyToRemove == null) {
      keyToRemove = findLeastRecentlyUsed();
    }

    if (keyToRemove != null) {
      logger.info("Evicting least valuable entry: {} (used {} times)",
              keyToRemove, cache.get(keyToRemove).getUsageCount());
      cache.remove(keyToRemove);
    }
  }

  /**
   * Finds the least recently used entry by expiration time (oldest).
   */
  private K findLeastRecentlyUsed() {
    K oldestKey = null;
    long oldestTime = Long.MAX_VALUE;

    for (Map.Entry<K, CacheEntry> entry : cache.entrySet()) {
      if (entry.getValue().expiryTime < oldestTime) {
        oldestTime = entry.getValue().expiryTime;
        oldestKey = entry.getKey();
      }
    }
    return oldestKey;
  }

  /**
   * Removes the mapping for a key from this cache if it is present.
   *
   * @param key the key whose mapping is to be removed from the cache
   */
  public void evict(K key) {
    synchronized (cache) {
      cache.remove(key);
      logger.debug("Cache evict for key: {}", key);
    }
  }

  /**
   * Returns the current number of entries in the cache.
   *
   * @return the number of entries currently in the cache
   */
  public int size() {
    synchronized (cache) {
      return cache.size();
    }
  }

  /**
   * Periodically evicts all expired entries from the cache.
   */
  private void evictExpiredEntries() {
    synchronized (cache) {
      int initialSize = cache.size();
      cache.entrySet().removeIf(entry -> {
        boolean expired = entry.getValue().isExpired();
        if (expired) {
          logger.debug("Cache entry evicted for key: {}", entry.getKey());
        }
        return expired;
      });
      if (initialSize != cache.size()) {
        logger.info("Evicted {} expired entries", initialSize - cache.size());
      }
    }
  }
}