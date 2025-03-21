package com.bank.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * A simple in-memory cache implementation using a thread-safe {@link ConcurrentHashMap}.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of values stored in this cache
 */
@Component
public class InMemoryCache<K, V> {

  private final Map<K, V> cache = new ConcurrentHashMap<>();

  /**
   * Retrieves a value from the cache by its key.
   *
   * @param key the key whose associated value is to be returned
   * @return the cached value, or {@code null} if the key is not found
   */
  public V get(K key) {
    return cache.get(key);
  }

  /**
   * Stores a value in the cache with the specified key.
   *
   * @param key   the key with which the specified value is to be associated
   * @param value the value to be stored in the cache
   */
  public void put(K key, V value) {
    cache.put(key, value);
  }

  /**
   * Removes a value from the cache by its key.
   *
   * @param key the key whose mapping is to be removed from the cache
   */
  public void evict(K key) {
    cache.remove(key);
  }

  /**
   * Clears all entries from the cache.
   */
  public void clear() {
    cache.clear();
  }
}
