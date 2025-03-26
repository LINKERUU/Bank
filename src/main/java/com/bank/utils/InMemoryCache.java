package com.bank.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A generic in-memory cache implementation with time-based eviction.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 */
@SuppressWarnings("squid:S6829")
@Component
public class InMemoryCache<K, V> {

  private static final Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

  /**
   * Internal cache entry that holds the value and its expiration time.
   *
   * @param <V> the type of the cached value
   */
  private record CacheEntry<V>(@Getter V value, long expiryTime) {

    /**
     * Checks if this cache entry has expired.
     *
     * @return true if the entry has expired, false otherwise
     */
    public boolean isExpired() {
      return System.currentTimeMillis() >= expiryTime;
    }
  }

  private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
  private final long ttlMillis;

  /**
   * Constructs a cache with default TTL of 5 minutes.
   */
  public InMemoryCache() {
    this(300_000); // Default TTL: 5 minutes
  }

  /**
   * Constructs a cache with specified time-to-live.
   *
   * @param ttlMillis the time-to-live for cache entries in milliseconds
   */
  public InMemoryCache(long ttlMillis) {
    this.ttlMillis = ttlMillis;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(this::evictExpiredEntries,
            ttlMillis, ttlMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Retrieves a value from the cache.
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is mapped, or null if not found or expired
   */
  public V get(K key) {
    CacheEntry<V> entry = cache.get(key);
    if (entry == null) {
      logger.info("Cache miss for key: {}", key);
      return null;
    }
    if (entry.isExpired()) {
      logger.info("Cache entry expired for key: {}", key);
      cache.remove(key);
      return null;
    }
    logger.info("Cache hit for key: {}", key);
    return entry.value();
  }

  /**
   * Associates the specified value with the specified key in this cache.
   *
   * @param key the key with which the specified value is to be associated
   * @param value the value to be associated with the specified key
   */
  public void put(K key, V value) {
    CacheEntry<V> entry = new CacheEntry<>(value, System.currentTimeMillis() + ttlMillis);
    cache.put(key, entry);
    logger.info("Cache put for key: {}", key);
  }

  /**
   * Removes the mapping for a key from this cache if it is present.
   *
   * @param key the key whose mapping is to be removed from the cache
   */
  public void evict(K key) {
    cache.remove(key);
    logger.info("Cache evict for key: {}", key);
  }

  /**
   * Periodically evicts all expired entries from the cache.
   */
  private void evictExpiredEntries() {
    for (Map.Entry<K, CacheEntry<V>> entry : cache.entrySet()) {
      if (entry.getValue().isExpired()) {
        cache.remove(entry.getKey());
        logger.info("Cache entry evicted for key: {}", entry.getKey());
      }
    }
  }
}